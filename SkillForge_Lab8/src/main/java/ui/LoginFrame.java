package ui;

import database.JsonDatabaseManager;
import models.*;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JsonDatabaseManager dbManager = JsonDatabaseManager.getInstance();
    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Skill Forge - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.add(new JLabel("Email:")); emailField = new JTextField(); formPanel.add(emailField);
        formPanel.add(new JLabel("Password:")); passwordField = new JPasswordField(); formPanel.add(passwordField);

        JPanel btnPanel = new JPanel();
        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Sign Up");
        btnPanel.add(loginBtn); btnPanel.add(signupBtn);

        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> handleLogin());
        signupBtn.addActionListener(e -> { new SignupFrame().setVisible(true); dispose(); });
    }

    private void handleLogin() {
        User user = dbManager.loginUser(emailField.getText(), new String(passwordField.getPassword()));
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid credentials.");
        } else {
            if (user instanceof Admin) new AdminDashboard((Admin)user).setVisible(true);
            else if (user instanceof Student) new StudentDashboardFrame((Student)user).setVisible(true);
            else if (user instanceof Instructor) new InstructorDashboardFrame((Instructor)user).setVisible(true);
            dispose();
        }
    }
}