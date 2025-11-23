package models;

import java.util.List;

public class Course {
    private String courseId;
    private String title;
    private String description;
    private String instructorId;
    private List<Lesson> lessons;
    private List<String> students; 
    private CourseStatus status;

    public Course(String courseId, String title, String description, String instructorId,
                  List<Lesson> lessons, List<String> students, CourseStatus status) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
        this.lessons = lessons;
        this.students = students;
        this.status = status;
    }

    public String getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getInstructorId() { return instructorId; }
    public List<Lesson> getLessons() { return lessons; }
    public List<String> getStudents() { return students; }
    public CourseStatus getStatus() { return status; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(CourseStatus status) { this.status = status; }

    @Override
    public String toString() {
        return title + " (" + status + ")";
    }
}