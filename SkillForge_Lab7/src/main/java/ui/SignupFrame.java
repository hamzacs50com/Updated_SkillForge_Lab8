package ui;

import database.JsonDatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

public class SignupFrame extends JFrame {

    private JsonDatabaseManager dbManager;
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton signupButton;
    private JButton backToLoginButton;

    // Basic email validation regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public SignupFrame() {
        this.dbManager = JsonDatabaseManager.getInstance();

        setTitle("Skill Forge - Sign Up");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Role:"));
        String[] roles = {"Student", "Instructor"};
        roleComboBox = new JComboBox<>(roles);
        formPanel.add(roleComboBox);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        signupButton = new JButton("Sign Up");
        backToLoginButton = new JButton("Back to Login");
        buttonPanel.add(signupButton);
        buttonPanel.add(backToLoginButton);
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSignup();
            }
        });

        backToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                SignupFrame.this.dispose();
            }
        });
    }

    private void handleSignup() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Call backend
        boolean success = dbManager.registerUser(username, email, password, role);

        if (success) {
            JOptionPane.showMessageDialog(this, "Signup successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error: Email already in use.", "Signup Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}