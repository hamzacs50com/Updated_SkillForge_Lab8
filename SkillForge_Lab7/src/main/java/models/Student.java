package models;

import java.util.List;
import java.util.Map;

public class Student extends User {

    private List<String> enrolledCourses; // List of courseIds
    private Map<String, List<String>> progress; // Map<courseId, List<lessonId>>

    public Student(String userId, String username, String email, String passwordHash, String role,
                   List<String> enrolledCourses, Map<String, List<String>> progress) {
        super(userId, username, email, passwordHash, role);
        this.enrolledCourses = enrolledCourses;
        this.progress = progress;
    }

    // Getters and Setters
    public List<String> getEnrolledCourses() { return enrolledCourses; }
    public Map<String, List<String>> getProgress() { return progress; }
    public void setEnrolledCourses(List<String> enrolledCourses) { this.enrolledCourses = enrolledCourses; }
    public void setProgress(Map<String, List<String>> progress) { this.progress = progress; }
}