package ui;

import database.JsonDatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class SignupFrame extends JFrame {
    private JsonDatabaseManager dbManager = JsonDatabaseManager.getInstance();
    private JTextField userField, emailField;
    private JPasswordField passField;
    private JComboBox<String> roleBox;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public SignupFrame() {
        setTitle("Skill Forge - Sign Up");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.add(new JLabel("Username:")); userField = new JTextField(); formPanel.add(userField);
        formPanel.add(new JLabel("Email:")); emailField = new JTextField(); formPanel.add(emailField);
        formPanel.add(new JLabel("Password:")); passField = new JPasswordField(); formPanel.add(passField);
        formPanel.add(new JLabel("Role:")); 
        roleBox = new JComboBox<>(new String[]{"Student", "Instructor", "Admin"});
        formPanel.add(roleBox);

        JPanel btnPanel = new JPanel();
        JButton signBtn = new JButton("Sign Up");
        JButton backBtn = new JButton("Back");
        btnPanel.add(signBtn); btnPanel.add(backBtn);
        
        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        signBtn.addActionListener(e -> {
            String email = emailField.getText();
            if (email.isEmpty() || userField.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Fields cannot be empty"); return; }
            if (!EMAIL_PATTERN.matcher(email).matches()) { JOptionPane.showMessageDialog(this, "Invalid email."); return; }
            
            if (dbManager.registerUser(userField.getText(), email, new String(passField.getPassword()), (String)roleBox.getSelectedItem())) {
                JOptionPane.showMessageDialog(this, "Success! Please log in.");
                new LoginFrame().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error: Email exists.");
            }
        });
        backBtn.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
    }
}