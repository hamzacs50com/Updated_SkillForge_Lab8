package models;

import java.util.List;

public class Quiz {
    private String quizId;
    private String lessonId;
    private List<Question> questions;

    public Quiz(String quizId, String lessonId, List<Question> questions) {
        this.quizId = quizId;
        this.lessonId = lessonId;
        this.questions = questions;
    }

    public String getQuizId() { return quizId; }
    public String getLessonId() { return lessonId; }
    public List<Question> getQuestions() { return questions; }
}