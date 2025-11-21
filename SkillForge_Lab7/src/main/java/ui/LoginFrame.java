package ui;

import database.JsonDatabaseManager;
import models.Instructor;
import models.Student;
import models.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {

    private JsonDatabaseManager dbManager;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;

    public LoginFrame() {
        this.dbManager = JsonDatabaseManager.getInstance();

        setTitle("Skill Forge - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel for the form inputs
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        // Panel for the buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        loginButton = new JButton("Login");
        signupButton = new JButton("Sign Up");
        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);

        // Add panels to frame
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Add title
        JLabel titleLabel = new JLabel("Welcome to Skill Forge", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // Add Action Listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SignupFrame signupFrame = new SignupFrame();
                signupFrame.setVisible(true);
                LoginFrame.this.dispose(); // Close this LoginFrame
            }
        });
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email and password cannot be empty.",
                                          "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = dbManager.loginUser(email, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid email or password.",
                                          "Login Failed", JOptionPane.ERROR_MESSAGE);
        } else {
            // Login successful!
            if (user instanceof Student) {
                StudentDashboardFrame studentDash = new StudentDashboardFrame((Student) user);
                studentDash.setVisible(true);
            } else if (user instanceof Instructor) {
                InstructorDashboardFrame instructorDash = new InstructorDashboardFrame((Instructor) user);
                instructorDash.setVisible(true);
            }
            this.dispose(); // Close the login window
        }
    }
}