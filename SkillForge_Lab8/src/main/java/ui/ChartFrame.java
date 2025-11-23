package ui;
import database.JsonDatabaseManager;
import models.Course;
import javax.swing.*;

public class ChartFrame extends JDialog {
    public ChartFrame(JFrame parent, Course c) {
        super(parent, "Insights", true);
        setSize(500, 300);
        setLocationRelativeTo(parent);
        java.util.Map<String, Double> s = JsonDatabaseManager.getInstance().getCourseStatistics(c.getCourseId());
        add(new JLabel("<html><center><h1>" + c.getTitle() + "</h1>Avg Score: " + s.get("avgQuizScore") + "%</center></html>", JLabel.CENTER));
    }
}