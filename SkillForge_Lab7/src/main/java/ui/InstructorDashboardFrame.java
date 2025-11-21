package ui;

import database.JsonDatabaseManager;
import models.Course;
import models.Instructor;
import models.Lesson;
import models.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class InstructorDashboardFrame extends JFrame {

    private JsonDatabaseManager dbManager;
    private Instructor instructor;

    // UI Components
    private JList<Course> courseList;
    private JList<Lesson> lessonList;
    private JList<Student> studentList;
    private DefaultListModel<Course> courseListModel;
    private DefaultListModel<Lesson> lessonListModel;
    private DefaultListModel<Student> studentListModel;
    
    private JButton createCourseButton;
    private JButton editCourseButton;
    private JButton deleteCourseButton;
    private JButton addLessonButton;
    private JButton editLessonButton;
    private JButton deleteLessonButton;

    public InstructorDashboardFrame(Instructor instructor) {
        this.dbManager = JsonDatabaseManager.getInstance();
        this.instructor = instructor;

        setTitle("Instructor Dashboard - Welcome, " + instructor.getUsername());
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Top Panel with Logout ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        topPanel.add(new JLabel("Instructor Dashboard", JLabel.CENTER), BorderLayout.CENTER);
        JButton logoutButton = new JButton("Logout");
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- Main Content Area ---
        // 1. Course List (Left)
        courseListModel = new DefaultListModel<>();
        courseList = new JList<>(courseListModel);
        courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane courseScrollPane = new JScrollPane(courseList);
        
        JPanel coursePanel = new JPanel(new BorderLayout());
        
        // Panel for course buttons
        JPanel courseButtonPanel = new JPanel(new FlowLayout());
        createCourseButton = new JButton("Create");
        editCourseButton = new JButton("Edit");
        deleteCourseButton = new JButton("Delete");
        courseButtonPanel.add(createCourseButton);
        courseButtonPanel.add(editCourseButton);
        courseButtonPanel.add(deleteCourseButton);
        
        coursePanel.add(courseScrollPane, BorderLayout.CENTER);
        coursePanel.add(courseButtonPanel, BorderLayout.SOUTH);
        coursePanel.setBorder(BorderFactory.createTitledBorder("My Courses"));

        // 2. Details (Right) - Using a JTabbedPane
        JTabbedPane detailsPane = new JTabbedPane();
        
        // 2a. Lessons Tab
        JPanel lessonPanel = new JPanel(new BorderLayout());
        lessonListModel = new DefaultListModel<>();
        lessonList = new JList<>(lessonListModel);
        JScrollPane lessonScrollPane = new JScrollPane(lessonList);
        
        // Panel for lesson buttons
        JPanel lessonButtonPanel = new JPanel(new FlowLayout());
        addLessonButton = new JButton("Add");
        editLessonButton = new JButton("Edit");
        deleteLessonButton = new JButton("Delete");
        lessonButtonPanel.add(addLessonButton);
        lessonButtonPanel.add(editLessonButton);
        lessonButtonPanel.add(deleteLessonButton);
        
        lessonPanel.add(lessonScrollPane, BorderLayout.CENTER);
        lessonPanel.add(lessonButtonPanel, BorderLayout.SOUTH);
        
        // 2b. Enrolled Students Tab
        JPanel studentPanel = new JPanel(new BorderLayout());
        studentListModel = new DefaultListModel<>();
        studentList = new JList<>(studentListModel);
        JScrollPane studentScrollPane = new JScrollPane(studentList);
        studentPanel.add(studentScrollPane, BorderLayout.CENTER);
        
        detailsPane.addTab("Lessons", lessonPanel);
        detailsPane.addTab("Enrolled Students", studentPanel);

        // --- Main Split Pane ---
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, coursePanel, detailsPane);
        mainSplitPane.setResizeWeight(0.45);
        add(mainSplitPane, BorderLayout.CENTER);
        
        // --- Load Initial Data ---
        refreshCourseList();

        // --- Action Listeners ---
        logoutButton.addActionListener(e -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            InstructorDashboardFrame.this.dispose();
        });

        createCourseButton.addActionListener(e -> handleCreateCourse());
        editCourseButton.addActionListener(e -> handleEditCourse());
        deleteCourseButton.addActionListener(e -> handleDeleteCourse());
        
        addLessonButton.addActionListener(e -> handleAddLesson());
        editLessonButton.addActionListener(e -> handleEditLesson());
        deleteLessonButton.addActionListener(e -> handleDeleteLesson());
        
        courseList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Course selectedCourse = courseList.getSelectedValue();
                if (selectedCourse != null) {
                    // Update both lessons and students tabs
                    refreshLessonList(selectedCourse);
                    refreshStudentList(selectedCourse);
                } else {
                    lessonListModel.clear();
                    studentListModel.clear();
                }
            }
        });
    }

    // --- Refresh Methods ---

    private void refreshCourseList() {
        courseListModel.clear();
        List<Course> courses = dbManager.getCoursesByInstructor(instructor.getUserId());
        for (Course course : courses) {
            courseListModel.addElement(course);
        }
    }
    
    private void refreshLessonList(Course course) {
        lessonListModel.clear();
        if (course != null && course.getLessons() != null) {
            for (Lesson lesson : course.getLessons()) {
                lessonListModel.addElement(lesson);
            }
        }
    }
    
    private void refreshStudentList(Course course) {
        studentListModel.clear();
        if (course != null) {
            List<Student> students = dbManager.getEnrolledStudents(course.getCourseId());
            for (Student student : students) {
                studentListModel.addElement(student);
            }
        }
    }

    // --- Course Action Handlers ---

    private void handleCreateCourse() {
        JTextField titleField = new JTextField(20);
        JTextArea descArea = new JTextArea(5, 20);
        JScrollPane descScrollPane = new JScrollPane(descArea);
        
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Title:"), BorderLayout.NORTH);
        panel.add(titleField, BorderLayout.CENTER);
        panel.add(new JLabel("Description:"), BorderLayout.SOUTH);

        JPanel panelMain = new JPanel(new BorderLayout(5, 5));
        panelMain.add(panel, BorderLayout.NORTH);
        panelMain.add(descScrollPane, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panelMain, "Create New Course", 
                                                   JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText();
            String description = descArea.getText();
            if (!title.isEmpty() && !description.isEmpty()) {
                dbManager.createCourse(title, description, instructor.getUserId());
                refreshCourseList();
            } else {
                JOptionPane.showMessageDialog(this, "Title and Description cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleEditCourse() {
        Course selectedCourse = courseList.getSelectedValue();
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField titleField = new JTextField(selectedCourse.getTitle(), 20);
        JTextArea descArea = new JTextArea(selectedCourse.getDescription(), 5, 20);
        JScrollPane descScrollPane = new JScrollPane(descArea);
        
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Title:"), BorderLayout.NORTH);
        panel.add(titleField, BorderLayout.CENTER);
        panel.add(new JLabel("Description:"), BorderLayout.SOUTH);

        JPanel panelMain = new JPanel(new BorderLayout(5, 5));
        panelMain.add(panel, BorderLayout.NORTH);
        panelMain.add(descScrollPane, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panelMain, "Edit Course", 
                                                   JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newTitle = titleField.getText();
            String newDescription = descArea.getText();
            if (!newTitle.isEmpty() && !newDescription.isEmpty()) {
                dbManager.updateCourse(selectedCourse.getCourseId(), newTitle, newDescription);
                refreshCourseList();
            } else {
                JOptionPane.showMessageDialog(this, "Title and Description cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleDeleteCourse() {
        Course selectedCourse = courseList.getSelectedValue();
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this course?\n" + selectedCourse.getTitle(),
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            dbManager.deleteCourse(selectedCourse.getCourseId());
            refreshCourseList();
            // Clear the other lists as the course is gone
            lessonListModel.clear();
            studentListModel.clear();
        }
    }
    
    // --- Lesson Action Handlers ---
    
    private void handleAddLesson() {
        Course selectedCourse = courseList.getSelectedValue();
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course first.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField titleField = new JTextField(20);
        JTextArea contentArea = new JTextArea(5, 20);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Lesson Title:"), BorderLayout.NORTH);
        panel.add(titleField, BorderLayout.CENTER);
        panel.add(new JLabel("Lesson Content:"), BorderLayout.SOUTH);

        JPanel panelMain = new JPanel(new BorderLayout(5, 5));
        panelMain.add(panel, BorderLayout.NORTH);
        panelMain.add(contentScrollPane, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panelMain, "Add New Lesson", 
                                                   JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText();
            String content = contentArea.getText();
            if (!title.isEmpty()) {
                dbManager.addLesson(selectedCourse.getCourseId(), title, content);
                refreshLessonList(selectedCourse);
            } else {
                JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleEditLesson() {
        Course selectedCourse = courseList.getSelectedValue();
        Lesson selectedLesson = lessonList.getSelectedValue();
        
        if (selectedCourse == null || selectedLesson == null) {
            JOptionPane.showMessageDialog(this, "Please select a course and a lesson to edit.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField titleField = new JTextField(selectedLesson.getTitle(), 20);
        JTextArea contentArea = new JTextArea(selectedLesson.getContent(), 5, 20);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Lesson Title:"), BorderLayout.NORTH);
        panel.add(titleField, BorderLayout.CENTER);
        panel.add(new JLabel("Lesson Content:"), BorderLayout.SOUTH);

        JPanel panelMain = new JPanel(new BorderLayout(5, 5));
        panelMain.add(panel, BorderLayout.NORTH);
        panelMain.add(contentScrollPane, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panelMain, "Edit Lesson", 
                                                   JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newTitle = titleField.getText();
            String newContent = contentArea.getText();
            if (!newTitle.isEmpty()) {
                dbManager.updateLesson(selectedCourse.getCourseId(), selectedLesson.getLessonId(), newTitle, newContent);
                refreshLessonList(selectedCourse);
            } else {
                JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleDeleteLesson() {
        Course selectedCourse = courseList.getSelectedValue();
        Lesson selectedLesson = lessonList.getSelectedValue();
        
        if (selectedCourse == null || selectedLesson == null) {
            JOptionPane.showMessageDialog(this, "Please select a course and a lesson to delete.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this lesson?\n" + selectedLesson.getTitle(),
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            dbManager.deleteLesson(selectedCourse.getCourseId(), selectedLesson.getLessonId());
            refreshLessonList(selectedCourse);
        }
    }
}