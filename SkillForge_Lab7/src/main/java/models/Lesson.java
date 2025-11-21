package models;

import java.util.List;

public class Lesson {

    private String lessonId;
    private String title;
    private String content;
    private List<String> resources;

    public Lesson(String lessonId, String title, String content, List<String> resources) {
        this.lessonId = lessonId;
        this.title = title;
        this.content = content;
        this.resources = resources;
    }

    // Getters and Setters
    public String getLessonId() { return lessonId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public List<String> getResources() { return resources; }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setResources(List<String> resources) { this.resources = resources; }

    @Override
    public String toString() {
        return title; // This is used to display nicely in JList
    }
}