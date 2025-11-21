package ui;

import database.JsonDatabaseManager;
import models.Course;
import models.Lesson;
import models.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

public class StudentDashboardFrame extends JFrame {

    private JsonDatabaseManager dbManager;
    private Student student;

    private JList<Course> allCoursesList;
    private JList<Course> myCoursesList;
    private JList<Lesson> lessonList;
    private JTextArea lessonContentArea;
    private DefaultListModel<Course> allCoursesModel;
    private DefaultListModel<Course> myCoursesModel;
    private DefaultListModel<Lesson> lessonListModel;

    public StudentDashboardFrame(Student student) {
        this.dbManager = JsonDatabaseManager.getInstance();
        this.student = student;

        setTitle("Student Dashboard - Welcome, " + student.getUsername());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Tabbed Pane ---
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // --- 1. Available Courses Tab ---
        JPanel allCoursesPanel = new JPanel(new BorderLayout(10, 10));
        allCoursesModel = new DefaultListModel<>();
        allCoursesList = new JList<>(allCoursesModel);
        allCoursesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane allCoursesScrollPane = new JScrollPane(allCoursesList);
        
        JButton enrollButton = new JButton("Enroll in Selected Course");
        
        allCoursesPanel.add(new JLabel("All Available Courses", JLabel.CENTER), BorderLayout.NORTH);
        allCoursesPanel.add(allCoursesScrollPane, BorderLayout.CENTER);
        allCoursesPanel.add(enrollButton, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Available Courses", allCoursesPanel);

        // --- 2. My Courses Tab ---
        JPanel myCoursesPanel = new JPanel(new BorderLayout(10, 10));
        myCoursesModel = new DefaultListModel<>();
        myCoursesList = new JList<>(myCoursesModel);
        myCoursesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane myCoursesScrollPane = new JScrollPane(myCoursesList);
        
        // Panel to show lessons for the selected course
        JPanel lessonPanel = new JPanel(new BorderLayout(5, 5));
        lessonPanel.setBorder(BorderFactory.createTitledBorder("Lessons"));
        
        lessonListModel = new DefaultListModel<>();
        lessonList = new JList<>(lessonListModel);
        JScrollPane lessonListScrollPane = new JScrollPane(lessonList);
        
        lessonContentArea = new JTextArea(10, 30);
        lessonContentArea.setEditable(false);
        lessonContentArea.setLineWrap(true);
        lessonContentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(lessonContentArea);
        
        JButton markCompletedButton = new JButton("Mark as Completed");

        JSplitPane lessonSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lessonListScrollPane, contentScrollPane);
        lessonSplitPane.setResizeWeight(0.4);
        
        lessonPanel.add(lessonSplitPane, BorderLayout.CENTER);
        lessonPanel.add(markCompletedButton, BorderLayout.SOUTH);

        // Main layout for "My Courses" tab
        JSplitPane myCoursesSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, myCoursesScrollPane, lessonPanel);
        myCoursesSplitPane.setResizeWeight(0.5);
        
        myCoursesPanel.add(new JLabel("My Enrolled Courses", JLabel.CENTER), BorderLayout.NORTH);
        myCoursesPanel.add(myCoursesSplitPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("My Courses", myCoursesPanel);

        // --- Logout Button ---
        JButton logoutButton = new JButton("Logout");
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(logoutButton);
        
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // --- Load Initial Data ---
        refreshAllCoursesList();
        refreshMyCoursesList();

        // --- Action Listeners ---
        
        enrollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Course selectedCourse = allCoursesList.getSelectedValue();
                if (selectedCourse != null) {
                    dbManager.enrollStudentInCourse(student.getUserId(), selectedCourse.getCourseId());
                    refreshAllCoursesList(); // Refresh both lists
                    refreshMyCoursesList();
                    JOptionPane.showMessageDialog(StudentDashboardFrame.this, "Enrolled in " + selectedCourse.getTitle());
                } else {
                    JOptionPane.showMessageDialog(StudentDashboardFrame.this, "Please select a course to enroll in.", "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        myCoursesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Course selectedCourse = myCoursesList.getSelectedValue();
                if (selectedCourse != null) {
                    refreshLessonList(selectedCourse);
                }
            }
        });
        
        lessonList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Lesson selectedLesson = lessonList.getSelectedValue();
                if (selectedLesson != null) {
                    // Display lesson content
                    String content = "Title: " + selectedLesson.getTitle() + "\n\n"
                                   + "Content: " + selectedLesson.getContent() + "\n\n"
                                   + "Resources: " + String.join(", ", selectedLesson.getResources());
                    lessonContentArea.setText(content);
                } else {
                    lessonContentArea.setText("");
                }
            }
        });
        
        markCompletedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Course selectedCourse = myCoursesList.getSelectedValue();
                Lesson selectedLesson = lessonList.getSelectedValue();
                if (selectedCourse != null && selectedLesson != null) {
                    dbManager.markLessonAsCompleted(student.getUserId(), selectedCourse.getCourseId(), selectedLesson.getLessonId());
                    JOptionPane.showMessageDialog(StudentDashboardFrame.this, "Lesson marked as completed!");
                    // You could add a visual indicator (e.g., change list renderer)
                    refreshLessonList(selectedCourse); // Refresh to update visuals
                } else {
                    JOptionPane.showMessageDialog(StudentDashboardFrame.this, "Please select a course and a lesson.", "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                StudentDashboardFrame.this.dispose();
            }
        });
    }

    private void refreshAllCoursesList() {
        allCoursesModel.clear();
        List<Course> allCourses = dbManager.getAllCourses();
        List<String> enrolledIds = student.getEnrolledCourses();
        // Only show courses the student is NOT already enrolled in
        for (Course course : allCourses) {
            if (!enrolledIds.contains(course.getCourseId())) {
                allCoursesModel.addElement(course);
            }
        }
    }

    private void refreshMyCoursesList() {
        myCoursesModel.clear();
        List<Course> myCourses = dbManager.getEnrolledCourses(student.getUserId());
        for (Course course : myCourses) {
            myCoursesModel.addElement(course);
        }
    }
    
    private void refreshLessonList(Course course) {
        lessonListModel.clear();
        lessonContentArea.setText("");
        
        Map<String, List<String>> progress = dbManager.getStudentById(student.getUserId()).getProgress();
        List<String> completedLessonIds = progress.get(course.getCourseId());
        
        if (course.getLessons() != null) {
            for (Lesson lesson : course.getLessons()) {
                lessonListModel.addElement(lesson);
            }
        }
        
        // This is how you add a custom renderer to show "COMPLETED"
        lessonList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Lesson lesson = (Lesson) value;
                if (completedLessonIds != null && completedLessonIds.contains(lesson.getLessonId())) {
                    setText(lesson.getTitle() + " (COMPLETED)");
                    setForeground(Color.GRAY);
                } else {
                    setText(lesson.getTitle());
                }
                return c;
            }
        });
    }
}