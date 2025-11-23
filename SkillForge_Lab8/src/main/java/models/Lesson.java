package models;

import java.util.List;

public class Lesson {
    private String lessonId;
    private String title;
    private String content;
    private List<String> resources;
    private Quiz quiz;

    public Lesson(String lessonId, String title, String content, List<String> resources, Quiz quiz) {
        this.lessonId = lessonId;
        this.title = title;
        this.content = content;
        this.resources = resources;
        this.quiz = quiz;
    }

    public String getLessonId() { return lessonId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public List<String> getResources() { return resources; }
    public Quiz getQuiz() { return quiz; }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    @Override
    public String toString() {
        return title + (quiz != null && !quiz.getQuestions().isEmpty() ? " (Has Quiz)" : "");
    }
}