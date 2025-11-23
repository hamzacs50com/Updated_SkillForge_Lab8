package ui;
import database.JsonDatabaseManager;
import models.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class QuizFrame extends JDialog {
    private int idx = 0, score = 0;
    private Quiz quiz;
    private JLabel qLabel = new JLabel("", JLabel.CENTER);
    private JPanel optPanel = new JPanel();
    private java.util.List<Integer> answers;

    public QuizFrame(JFrame p, Student s, Course c, Lesson l) {
        super(p, "Quiz", true);
        this.quiz = l.getQuiz();
        this.answers = new ArrayList<>(java.util.Collections.nCopies(quiz.getQuestions().size(), -1));
        setSize(500, 400); setLocationRelativeTo(p); setLayout(new BorderLayout());
        
        JButton next = new JButton("Next");
        add(qLabel, BorderLayout.NORTH);
        add(new JScrollPane(optPanel), BorderLayout.CENTER);
        add(next, BorderLayout.SOUTH);
        optPanel.setLayout(new BoxLayout(optPanel, BoxLayout.Y_AXIS));

        loadQ(0, next);
        next.addActionListener(e -> {
            int sel = -1;
            for(int i=0; i<optPanel.getComponentCount(); i++) if(((JRadioButton)optPanel.getComponent(i)).isSelected()) sel = i;
            if(sel == -1) return;
            answers.set(idx, sel);
            if(++idx < quiz.getQuestions().size()) loadQ(idx, next);
            else {
                for(int i=0; i<quiz.getQuestions().size(); i++) if(answers.get(i) == quiz.getQuestions().get(i).getCorrectOptionIndex()) score++;
                int pct = (int)(((double)score/quiz.getQuestions().size())*100);
                JsonDatabaseManager.getInstance().submitQuiz(s.getUserId(), c.getCourseId(), l.getLessonId(), pct);
                JOptionPane.showMessageDialog(this, "Score: " + pct + "%");
                dispose();
            }
        });
    }
    private void loadQ(int i, JButton b) {
        Question q = quiz.getQuestions().get(i);
        qLabel.setText((i+1) + ". " + q.getQuestionText());
        optPanel.removeAll();
        ButtonGroup g = new ButtonGroup();
        for(String o : q.getOptions()) { JRadioButton r = new JRadioButton(o); optPanel.add(r); g.add(r); }
        if(i == quiz.getQuestions().size()-1) b.setText("Submit");
        optPanel.revalidate(); optPanel.repaint();
    }
}