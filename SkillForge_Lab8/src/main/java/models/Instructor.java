package models;

import java.util.List;

public class Instructor extends User {
    private List<String> createdCourses;

    public Instructor(String userId, String username, String email, String passwordHash, String role, List<String> createdCourses) {
        super(userId, username, email, passwordHash, role);
        this.createdCourses = createdCourses;
    }

    public List<String> getCreatedCourses() { return createdCourses; }
}