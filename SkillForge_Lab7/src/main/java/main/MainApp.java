package main;

import ui.LoginFrame;

import javax.swing.*;

/**
 * Main application class.
 * This is the entry point that starts the application.
 */
public class MainApp {

    public static void main(String[] args) {
        // Run the Swing GUI on the Event Dispatch Thread for thread-safety
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Create and display the LoginFrame
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}