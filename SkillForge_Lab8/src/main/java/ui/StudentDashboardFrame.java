package ui;

import database.JsonDatabaseManager;
import models.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StudentDashboardFrame extends JFrame {
    private JsonDatabaseManager db = JsonDatabaseManager.getInstance();
    private Student student;
    private DefaultListModel<Course> allModel = new DefaultListModel<>();
    private DefaultListModel<Course> myModel = new DefaultListModel<>();
    private DefaultListModel<Lesson> lModel = new DefaultListModel<>();
    private DefaultListModel<Certificate> certModel = new DefaultListModel<>();
    private JList<Course> allList = new JList<>(allModel);
    private JList<Course> myList = new JList<>(myModel);
    private JList<Lesson> lList = new JList<>(lModel);
    private JTextArea content = new JTextArea();
    private JButton takeQuiz = new JButton("Take Quiz"), unmark = new JButton("Unmark");

    public StudentDashboardFrame(Student s) {
        this.student = s;
        setTitle("Student - " + s.getUsername());
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        
        JPanel p1 = new JPanel(new BorderLayout());
        JButton enroll = new JButton("Enroll");
        p1.add(new JScrollPane(allList), BorderLayout.CENTER); p1.add(enroll, BorderLayout.SOUTH);
        tabs.addTab("Available", p1);

        JPanel p2 = new JPanel(new BorderLayout());
        JPanel lPanel = new JPanel(new BorderLayout());
        JPanel bPanel = new JPanel();
        bPanel.add(takeQuiz); bPanel.add(unmark);
        takeQuiz.setEnabled(false); unmark.setEnabled(false);
        lPanel.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(lList), new JScrollPane(content)), BorderLayout.CENTER);
        lPanel.add(bPanel, BorderLayout.SOUTH);
        p2.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(myList), lPanel), BorderLayout.CENTER);
        tabs.addTab("My Courses", p2);

        tabs.addTab("Certificates", new JScrollPane(new JList<>(certModel)));

        JButton logout = new JButton("Logout");
        add(logout, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        refresh();

        logout.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
        enroll.addActionListener(e -> { 
            if(allList.getSelectedValue()!=null) { 
                db.enrollStudentInCourse(student.getUserId(), allList.getSelectedValue().getCourseId()); 
                refresh(); 
            } 
        });
        
        myList.addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) {
                lModel.clear();
                Course c = myList.getSelectedValue();
                if(c!=null && c.getLessons()!=null) for(Lesson l : c.getLessons()) lModel.addElement(l);
            }
        });
        
        lList.addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) {
                Lesson l = lList.getSelectedValue();
                if(l!=null) {
                    content.setText(l.getContent());
                    boolean done = student.isLessonCompleted(l.getLessonId());
                    takeQuiz.setEnabled(l.getQuiz()!=null);
                    unmark.setEnabled(done);
                }
            }
        });
        
        takeQuiz.addActionListener(e -> {
            Course c = myList.getSelectedValue(); Lesson l = lList.getSelectedValue();
            if(c!=null && l!=null && l.getQuiz() != null) {
                new QuizFrame(this, student, c, l).setVisible(true);
                refresh();
            }
        });
        
        unmark.addActionListener(e -> {
            Lesson l = lList.getSelectedValue();
            if(l!=null && JOptionPane.showConfirmDialog(this, "Reset progress?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                db.unmarkLessonAsCompleted(student.getUserId(), l.getLessonId());
                refresh();
            }
        });
    }

    private void refresh() {
        this.student = db.getStudentById(student.getUserId());
        allModel.clear(); myModel.clear(); certModel.clear();
        for(Course c : db.getApprovedCourses()) if(!student.getEnrolledCourses().contains(c.getCourseId())) allModel.addElement(c);
        for(Course c : db.getEnrolledCourses(student.getUserId())) myModel.addElement(c);
        for(Certificate c : db.getCertificates(student.getUserId())) certModel.addElement(c);
        
        lList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l,v,i,s,f);
                if (student.isLessonCompleted(((Lesson)v).getLessonId())) { setText(((Lesson)v).getTitle() + " (DONE)"); setForeground(Color.GRAY); }
                return this;
            }
        });
    }
}