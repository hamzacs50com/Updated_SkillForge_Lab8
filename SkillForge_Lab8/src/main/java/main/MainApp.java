package main;

import ui.LoginFrame;
import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}