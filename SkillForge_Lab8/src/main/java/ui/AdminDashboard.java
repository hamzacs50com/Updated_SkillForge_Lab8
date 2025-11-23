package ui;

import database.JsonDatabaseManager;
import models.*;
import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    private JsonDatabaseManager dbManager = JsonDatabaseManager.getInstance();
    private DefaultListModel<Course> model = new DefaultListModel<>();
    private JList<Course> list = new JList<>(model);

    public AdminDashboard(Admin admin) {
        setTitle("Admin Dashboard");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        JButton logout = new JButton("Logout");
        top.add(new JLabel("Pending Courses", JLabel.CENTER), BorderLayout.CENTER);
        top.add(logout, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton approve = new JButton("Approve");
        JButton reject = new JButton("Reject");
        btnPanel.add(approve); btnPanel.add(reject);
        add(btnPanel, BorderLayout.SOUTH);

        refresh();

        logout.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
        approve.addActionListener(e -> { if (list.getSelectedValue() != null) { dbManager.approveCourse(list.getSelectedValue().getCourseId()); refresh(); } });
        reject.addActionListener(e -> { if (list.getSelectedValue() != null) { dbManager.rejectCourse(list.getSelectedValue().getCourseId()); refresh(); } });
    }
    private void refresh() { model.clear(); for(Course c : dbManager.getPendingCourses()) model.addElement(c); }
}