package ui;

import database.JsonDatabaseManager;
import models.Course;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ChartFrame extends JDialog {

    public ChartFrame(JFrame parent, Course course) {
        super(parent, "Insights: " + course.getTitle(), true);
        setSize(600, 450);
        setLocationRelativeTo(parent);
        
        // 1. Get REAL stats from the database
        Map<String, Double> stats = JsonDatabaseManager.getInstance().getCourseStatistics(course.getCourseId());
        double avgScore = stats.get("avgQuizScore");
        double avgComp = stats.get("avgCompletion");

        setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Performance Analytics: " + course.getTitle(), JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(title, BorderLayout.NORTH);

        // 2. Add the custom painted bar chart panel
        add(new BarChartPanel(avgScore, avgComp), BorderLayout.CENTER);
    }

    // --- Custom Panel to Draw the Chart ---
    private static class BarChartPanel extends JPanel {
        private double score;
        private double comp;

        public BarChartPanel(double score, double comp) {
            this.score = score;
            this.comp = comp;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Use Graphics2D for smoother lines
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            
            // Chart dimensions
            int barWidth = 80;
            int chartBottom = h - 50;
            int chartTop = 50;
            int maxHeight = chartBottom - chartTop;

            // Draw Axis Lines
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(50, chartBottom, w - 50, chartBottom); // X-Axis
            g2.drawLine(50, chartBottom, 50, chartTop);        // Y-Axis

            // --- Bar 1: Avg Score (Blue) ---
            int scoreHeight = (int) ((score / 100.0) * maxHeight);
            int x1 = (w / 3) - (barWidth / 2);
            int y1 = chartBottom - scoreHeight;
            
            g2.setColor(new Color(70, 130, 180)); // Steel Blue
            g2.fillRect(x1, y1, barWidth, scoreHeight);
            g2.setColor(Color.BLACK);
            g2.drawRect(x1, y1, barWidth, scoreHeight);
            
            // Text for Bar 1
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("Avg Quiz Score", x1 - 10, chartBottom + 20);
            g2.drawString(String.format("%.1f%%", score), x1 + 20, y1 - 5);

            // --- Bar 2: Completion (Green) ---
            int compHeight = (int) ((comp / 100.0) * maxHeight);
            int x2 = (2 * w / 3) - (barWidth / 2);
            int y2 = chartBottom - compHeight;
            
            g2.setColor(new Color(60, 179, 113)); // Medium Sea Green
            g2.fillRect(x2, y2, barWidth, compHeight);
            g2.setColor(Color.BLACK);
            g2.drawRect(x2, y2, barWidth, compHeight);

            // Text for Bar 2
            g2.drawString("Completion Rate", x2 - 10, chartBottom + 20);
            g2.drawString(String.format("%.1f%%", comp), x2 + 20, y2 - 5);
        }
    }
}