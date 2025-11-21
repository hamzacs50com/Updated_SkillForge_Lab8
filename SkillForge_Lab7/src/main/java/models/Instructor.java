package models;

import java.util.List;

public class Instructor extends User {

    private List<String> createdCourses; // List of courseIds

    public Instructor(String userId, String username, String email, String passwordHash, String role,
                      List<String> createdCourses) {
        super(userId, username, email, passwordHash, role);
        this.createdCourses = createdCourses;
    }

    // Getters and Setters
    public List<String> getCreatedCourses() { return createdCourses; }
    public void setCreatedCourses(List<String> createdCourses) { this.createdCourses = createdCourses; }
}