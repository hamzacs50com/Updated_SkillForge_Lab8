
import database.JsonDatabaseManager;
import models.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InstructorDashboardFrame extends JFrame {
    private JsonDatabaseManager db = JsonDatabaseManager.getInstance();
    private Instructor instructor;
    private DefaultListModel<Course> cModel = new DefaultListModel<>();
    private JList<Course> cList = new JList<>(cModel);
    private DefaultListModel<Lesson> lModel = new DefaultListModel<>();
    private JList<Lesson> lList = new JList<>(lModel);
    private DefaultListModel<Student> sModel = new DefaultListModel<>();
    private JList<Student> sList = new JList<>(sModel);

    public InstructorDashboardFrame(Instructor i) {
        this.instructor = i;
        setTitle("Instructor - " + i.getUsername());
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        JButton logout = new JButton("Logout");
        top.add(new JLabel("Instructor Dashboard", JLabel.CENTER));
        top.add(logout, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel cPanel = new JPanel(new BorderLayout());
        JPanel cBtn = new JPanel();
        JButton cAdd = new JButton("Create"), cEdit = new JButton("Edit"), cDel = new JButton("Delete");
        cBtn.add(cAdd); cBtn.add(cEdit); cBtn.add(cDel);
        cPanel.add(new JScrollPane(cList), BorderLayout.CENTER);
        cPanel.add(cBtn, BorderLayout.SOUTH);
        cPanel.setBorder(BorderFactory.createTitledBorder("Courses"));

        JTabbedPane tabs = new JTabbedPane();
        JPanel lPanel = new JPanel(new BorderLayout());
        JPanel lBtn = new JPanel();
        JButton lAdd = new JButton("Add"), lEdit = new JButton("Edit"), lDel = new JButton("Delete");
        lBtn.add(lAdd); lBtn.add(lEdit); lBtn.add(lDel);
        lPanel.add(new JScrollPane(lList), BorderLayout.CENTER);
        lPanel.add(lBtn, BorderLayout.SOUTH);
        tabs.addTab("Lessons", lPanel);
        tabs.addTab("Students", new JScrollPane(sList));
        
        JPanel iPanel = new JPanel(new BorderLayout());
        JButton vChart = new JButton("View Charts");
        iPanel.add(new JLabel("Select a course for insights", JLabel.CENTER), BorderLayout.CENTER);
        iPanel.add(vChart, BorderLayout.SOUTH);
        tabs.addTab("Insights", iPanel);

        add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cPanel, tabs), BorderLayout.CENTER);

        refreshC();
        
        logout.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
        cList.addListSelectionListener(e -> { if(!e.getValueIsAdjusting()) refreshLS(); });
        
        // --- COURSE ACTIONS ---
        cAdd.addActionListener(e -> {
            String t = JOptionPane.showInputDialog("Title:");
            String d = JOptionPane.showInputDialog("Desc:");
            if(t!=null && d!=null) { db.createCourse(t,d,i.getUserId()); refreshC(); }
        });
        cEdit.addActionListener(e -> {
            Course c = cList.getSelectedValue();
            if(c!=null) { 
                String t = JOptionPane.showInputDialog("Title:", c.getTitle());
                String d = JOptionPane.showInputDialog("Desc:", c.getDescription());
                if(t!=null) { db.updateCourse(c.getCourseId(), t, d); refreshC(); }
            }
        });
        cDel.addActionListener(e -> {
            Course c = cList.getSelectedValue();
            if(c!=null && JOptionPane.showConfirmDialog(this,"Delete?")==0) { db.deleteCourse(c.getCourseId()); refreshC(); }
        });
        
        // --- LESSON ACTIONS (VIP VERSION: MANUAL QUIZ) ---
        lAdd.addActionListener(e -> handleAddLesson());
        lEdit.addActionListener(e -> handleEditLesson());
        lDel.addActionListener(e -> {
            Course c = cList.getSelectedValue(); Lesson l = lList.getSelectedValue();
            if(c!=null && l!=null && JOptionPane.showConfirmDialog(this,"Delete Lesson?")==0) { 
                db.deleteLesson(c.getCourseId(), l.getLessonId()); refreshLS(); 
            }
        });
        
        vChart.addActionListener(e -> { if(cList.getSelectedValue()!=null) new ChartFrame(this, cList.getSelectedValue()).setVisible(true); });
    }
    
    // --- HELPER METHODS ---
    private void refreshC() { 
        cModel.clear(); 
        for(Course c : db.getCoursesByInstructor(instructor.getUserId())) cModel.addElement(c); 
        cList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l,v,i,s,f);
                Course c = (Course)v;
                if(c.getStatus() == CourseStatus.REJECTED) setForeground(Color.RED);
                else if(c.getStatus() == CourseStatus.PENDING) setForeground(Color.BLUE);
                return this;
            }
        });
    }
    private void refreshLS() {
        lModel.clear(); sModel.clear();
        Course c = cList.getSelectedValue();
        if(c!=null) {
            // RELOAD COURSE FROM DB TO GET LATEST DATA
            Course refreshedCourse = db.getCourseById(c.getCourseId());
            if(refreshedCourse.getLessons()!=null) for(Lesson l : refreshedCourse.getLessons()) lModel.addElement(l);
            for(Student s : db.getEnrolledStudents(refreshedCourse.getCourseId())) sModel.addElement(s);
        }
    }

    // --- MANUAL QUIZ BUILDER LOGIC ---
    
    private void handleAddLesson() {
        Course c = cList.getSelectedValue();
        if(c==null) { JOptionPane.showMessageDialog(this, "Select a course"); return; }
        
        JTextField tField = new JTextField();
        JTextArea cArea = new JTextArea(5, 20);
        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(new JLabel("Title:")); p.add(tField);
        p.add(new JLabel("Content:")); p.add(new JScrollPane(cArea));
        
        int res = JOptionPane.showConfirmDialog(this, p, "Add Lesson", JOptionPane.OK_CANCEL_OPTION);
        if(res == JOptionPane.OK_OPTION && !tField.getText().isEmpty()) {
            // Ask to build quiz
            Quiz q = null;
            if(JOptionPane.showConfirmDialog(this, "Create a Quiz for this lesson?", "Quiz", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                q = buildQuiz();
            }
            db.addLesson(c.getCourseId(), tField.getText(), cArea.getText(), q);
            refreshLS();
        }
    }

    private void handleEditLesson() {
        Course c = cList.getSelectedValue();
        Lesson l = lList.getSelectedValue();
        if(c==null || l==null) { JOptionPane.showMessageDialog(this, "Select a lesson"); return; }
        
        JTextField tField = new JTextField(l.getTitle());
        JTextArea cArea = new JTextArea(l.getContent(), 5, 20);
        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(new JLabel("Title:")); p.add(tField);
        p.add(new JLabel("Content:")); p.add(new JScrollPane(cArea));
        
        int res = JOptionPane.showConfirmDialog(this, p, "Edit Lesson", JOptionPane.OK_CANCEL_OPTION);
        if(res == JOptionPane.OK_OPTION && !tField.getText().isEmpty()) {
            Quiz q = null; // Default: don't update quiz
            // Check if user wants to replace quiz
            if(JOptionPane.showConfirmDialog(this, "Replace/Update Quiz?", "Quiz", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                q = buildQuiz();
            }
            // If q is null, it means we don't change the quiz, UNLESS we explicitly deleted it.
            // For simplicity in this lab: If they say "No" to replacing, we pass null, and DB manager keeps old quiz.
            // If they say "Yes" and build a new one, we pass the new one.
            db.updateLesson(c.getCourseId(), l.getLessonId(), tField.getText(), cArea.getText(), q);
            refreshLS();
        }
    }

    // The Logic to manually add questions
    private Quiz buildQuiz() {
        List<Question> questions = new ArrayList<>();
        while(true) {
            String qText = JOptionPane.showInputDialog("Enter Question Text (or Cancel to finish):");
            if(qText == null || qText.trim().isEmpty()) break;
            
            String opt1 = JOptionPane.showInputDialog("Option 1:");
            String opt2 = JOptionPane.showInputDialog("Option 2:");
            String opt3 = JOptionPane.showInputDialog("Option 3:");
            String correctStr = JOptionPane.showInputDialog("Correct Option Index (1, 2, or 3):");
            
            if(opt1!=null && opt2!=null && opt3!=null && correctStr!=null) {
                try {
                    int correctIdx = Integer.parseInt(correctStr) - 1; // Convert 1-based to 0-based
                    if(correctIdx < 0 || correctIdx > 2) correctIdx = 0;
                    
                    List<String> opts = new ArrayList<>();
                    opts.add(opt1); opts.add(opt2); opts.add(opt3);
                    questions.add(new Question(qText, opts, correctIdx));
                } catch(NumberFormatException e) {}
            }
            
            if(JOptionPane.showConfirmDialog(this, "Add another question?", "Next", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) break;
        }
        if(questions.isEmpty()) return null;
        return new Quiz(UUID.randomUUID().toString(), "", questions);
    }
}