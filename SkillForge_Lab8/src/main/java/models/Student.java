package models;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class Student extends User {
    private List<String> enrolledCourses;
    private Map<String, Integer> quizScores; // Map<lessonId, score>
    private List<Certificate> certificates;

    public Student(String userId, String username, String email, String passwordHash, String role,
                   List<String> enrolledCourses, Map<String, Integer> quizScores, List<Certificate> certificates) {
        super(userId, username, email, passwordHash, role);
        this.enrolledCourses = enrolledCourses;
        this.quizScores = (quizScores != null) ? quizScores : new HashMap<>();
        this.certificates = (certificates != null) ? certificates : new ArrayList<>();
    }

    public List<String> getEnrolledCourses() { return enrolledCourses; }
    public Map<String, Integer> getQuizScores() { return quizScores; }
    public List<Certificate> getCertificates() { return certificates; }
    
    public boolean isLessonCompleted(String lessonId) {
        // Passed if score is 50 or higher
        return quizScores.containsKey(lessonId) && quizScores.get(lessonId) >= 50;
    }
    
    public void addCertificate(Certificate cert) {
        this.certificates.add(cert);
    }
}