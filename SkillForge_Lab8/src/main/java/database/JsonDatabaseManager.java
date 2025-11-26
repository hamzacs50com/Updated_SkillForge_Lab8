package database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import models.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonDatabaseManager {

    private static JsonDatabaseManager instance;
    private static final String USERS_FILE = "data/users.json";
    private static final String COURSES_FILE = "data/courses.json";

    private List<User> users;
    private List<Course> courses;
    private Gson gson;

    private JsonDatabaseManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        loadCourses();
        loadUsers();
        
        if (getUserByEmail("admin@skillforge.com") == null) {
            String adminId = UUID.randomUUID().toString();
            String adminPass = PasswordHasher.hashPassword("admin123");
            Admin admin = new Admin(adminId, "Admin", "admin@skillforge.com", adminPass, "Admin");
            users.add(admin);
            saveUsers();
        }
    }

    public static synchronized JsonDatabaseManager getInstance() {
        if (instance == null) instance = new JsonDatabaseManager();
        return instance;
    }

    // --- IO ---
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        users = new ArrayList<>();
        try (FileReader reader = new FileReader(USERS_FILE)) {
            Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> userMaps = gson.fromJson(reader, type);
            if (userMaps == null) return;
            for (Map<String, Object> userMap : userMaps) {
                String role = (String) userMap.get("role");
                if ("Student".equals(role)) users.add(gson.fromJson(gson.toJson(userMap), Student.class));
                else if ("Instructor".equals(role)) users.add(gson.fromJson(gson.toJson(userMap), Instructor.class));
                else if ("Admin".equals(role)) users.add(gson.fromJson(gson.toJson(userMap), Admin.class));
            }
        } catch (IOException e) { System.out.println("users.json not found, creating new."); }
    }

    private synchronized void saveUsers() {
        try (FileWriter writer = new FileWriter(USERS_FILE)) { gson.toJson(users, writer); } 
        catch (IOException e) { e.printStackTrace(); }
    }

    private void loadCourses() {
        try (FileReader reader = new FileReader(COURSES_FILE)) {
            Type type = new TypeToken<List<Course>>() {}.getType();
            courses = gson.fromJson(reader, type);
            if (courses == null) courses = new ArrayList<>();
        } catch (IOException e) { 
            System.out.println("courses.json not found, creating new.");
            courses = new ArrayList<>();
        }
    }

    private synchronized void saveCourses() {
        try (FileWriter writer = new FileWriter(COURSES_FILE)) { gson.toJson(courses, writer); } 
        catch (IOException e) { e.printStackTrace(); }
    }

    // --- Auth ---
    public User loginUser(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && PasswordHasher.checkPassword(password, user.getPasswordHash())) {
                return user;
            }
        }
        return null;
    }
    public User getUserByEmail(String email) {
        for (User user : users) if (user.getEmail().equalsIgnoreCase(email)) return user;
        return null;
    }
    public boolean registerUser(String username, String email, String password, String role) {
        if (getUserByEmail(email) != null) return false;
        String userId = UUID.randomUUID().toString();
        String passwordHash = PasswordHasher.hashPassword(password);
        User newUser;
        if ("Student".equals(role)) newUser = new Student(userId, username, email, passwordHash, role, new ArrayList<>(), new HashMap<>(), new ArrayList<>());
        else if ("Instructor".equals(role)) newUser = new Instructor(userId, username, email, passwordHash, role, new ArrayList<>());
        else newUser = new Admin(userId, username, email, passwordHash, role);
        users.add(newUser);
        saveUsers();
        return true;
    }

    // --- Student ---
    public List<Course> getApprovedCourses() {
        return courses.stream().filter(c -> c.getStatus() == CourseStatus.APPROVED).collect(Collectors.toList());
    }
    public List<Course> getEnrolledCourses(String studentId) {
        Student student = getStudentById(studentId);
        if (student == null) return new ArrayList<>();
        return courses.stream().filter(c -> c.getStatus() == CourseStatus.APPROVED && student.getEnrolledCourses().contains(c.getCourseId())).collect(Collectors.toList());
    }
    public void enrollStudentInCourse(String studentId, String courseId) {
        Student student = getStudentById(studentId);
        Course course = getCourseById(courseId);
        if (student != null && course != null && course.getStatus() == CourseStatus.APPROVED) {
            if (!student.getEnrolledCourses().contains(courseId)) student.getEnrolledCourses().add(courseId);
            if (!course.getStudents().contains(studentId)) course.getStudents().add(studentId);
            saveUsers(); saveCourses();
        }
    }

    // --- Instructor ---
    public List<Course> getCoursesByInstructor(String instructorId) {
        return courses.stream().filter(c -> c.getInstructorId().equals(instructorId)).collect(Collectors.toList());
    }
    public List<Student> getEnrolledStudents(String courseId) {
        Course course = getCourseById(courseId);
        if (course == null) return new ArrayList<>();
        return users.stream().filter(u -> u instanceof Student && course.getStudents().contains(u.getUserId())).map(u -> (Student)u).collect(Collectors.toList());
    }
    public void createCourse(String title, String description, String instructorId) {
        String courseId = UUID.randomUUID().toString();
        Course newCourse = new Course(courseId, title, description, instructorId, new ArrayList<>(), new ArrayList<>(), CourseStatus.PENDING);
        courses.add(newCourse);
        Instructor instructor = getInstructorById(instructorId);
        if (instructor != null) { instructor.getCreatedCourses().add(courseId); saveUsers(); }
        saveCourses();
    }
    
    public void addLesson(String courseId, String title, String content, Quiz quiz) {
        Course course = getCourseById(courseId);
        if (course != null) { 
            String lessonId = UUID.randomUUID().toString();
            Lesson newLesson = new Lesson(lessonId, title, content, new ArrayList<>(), quiz);
            course.getLessons().add(newLesson);
            saveCourses(); 
        }
    }

    public void updateCourse(String id, String title, String desc) {
        Course c = getCourseById(id);
        if (c != null) { c.setTitle(title); c.setDescription(desc); saveCourses(); }
    }
    
    public void deleteCourse(String courseId) {
        Course c = getCourseById(courseId);
        if (c == null) return;
        courses.remove(c);
        for (User user : users) {
            if (user instanceof Instructor) ((Instructor)user).getCreatedCourses().remove(courseId);
            else if (user instanceof Student) {
                Student s = (Student)user;
                s.getEnrolledCourses().remove(courseId);
                s.getCertificates().removeIf(cert -> cert.getCourseId().equals(courseId));
                if (c.getLessons() != null) for (Lesson l : c.getLessons()) s.getQuizScores().remove(l.getLessonId());
            }
        }
        saveCourses(); saveUsers();
    }
    
    public void deleteLesson(String cId, String lId) {
        Course c = getCourseById(cId);
        if (c != null) { c.getLessons().removeIf(l -> l.getLessonId().equals(lId)); saveCourses(); }
    }
    
    public void updateLesson(String cId, String lId, String title, String content, Quiz newQuiz) {
        Course c = getCourseById(cId);
        if (c != null) {
            for (Lesson l : c.getLessons()) {
                if (l.getLessonId().equals(lId)) { 
                    l.setTitle(title); l.setContent(content);
                    if (newQuiz != null) l.setQuiz(newQuiz);
                    saveCourses(); return; 
                }
            }
        }
    }

    // --- Admin ---
    public List<Course> getPendingCourses() { return courses.stream().filter(c -> c.getStatus() == CourseStatus.PENDING).collect(Collectors.toList()); }
    public void approveCourse(String id) { Course c = getCourseById(id); if (c != null) { c.setStatus(CourseStatus.APPROVED); saveCourses(); } }
    public void rejectCourse(String id) { Course c = getCourseById(id); if (c != null) { c.setStatus(CourseStatus.REJECTED); saveCourses(); } }

    // --- Quiz & Certificate ---
    public void submitQuiz(String sId, String cId, String lId, int score) {
        Student s = getStudentById(sId);
        if (s == null) return;
        Integer old = s.getQuizScores().get(lId);
        if (old == null || score > old) s.getQuizScores().put(lId, score);
        saveUsers();
        if (score >= 50) checkCourseCompletion(sId, cId);
    }
    public void unmarkLessonAsCompleted(String sId, String lId) {
        Student s = getStudentById(sId);
        if (s != null && s.getQuizScores().containsKey(lId)) { s.getQuizScores().remove(lId); saveUsers(); }
    }
    private void checkCourseCompletion(String sId, String cId) {
        Student s = getStudentById(sId); Course c = getCourseById(cId);
        if (s == null || c == null) return;
        for (Certificate cert : s.getCertificates()) if (cert.getCourseId().equals(cId)) return;
        for (Lesson l : c.getLessons()) if (l.getQuiz() != null && !s.isLessonCompleted(l.getLessonId())) return;
        String certId = UUID.randomUUID().toString();
        s.addCertificate(new Certificate(certId, s.getUserId(), s.getUsername(), c.getCourseId(), c.getTitle(), LocalDate.now().toString()));
        saveUsers();
    }
    public List<Certificate> getCertificates(String sId) {
        Student s = getStudentById(sId);
        return s != null ? s.getCertificates() : new ArrayList<>();
    }
    
    // --- REAL ANALYTICS LOGIC (Fixed) ---
    public Map<String, Double> getCourseStatistics(String cId) {
        Map<String, Double> stats = new HashMap<>();
        Course course = getCourseById(cId);
        List<Student> students = getEnrolledStudents(cId);
        
        if (course == null || students.isEmpty() || course.getLessons() == null) {
            stats.put("avgCompletion", 0.0);
            stats.put("avgQuizScore", 0.0);
            return stats;
        }

        double totalQuizScore = 0;
        int quizCount = 0;
        int completedLessons = 0;
        int totalLessonsPossible = students.size() * course.getLessons().size();

        for (Student s : students) {
            for (Lesson l : course.getLessons()) {
                if (s.isLessonCompleted(l.getLessonId())) {
                    completedLessons++;
                    Integer score = s.getQuizScores().get(l.getLessonId());
                    if (score != null) {
                        totalQuizScore += score;
                        quizCount++;
                    }
                }
            }
        }

        double avgScore = (quizCount > 0) ? (totalQuizScore / quizCount) : 0.0;
        double avgComp = (totalLessonsPossible > 0) ? ((double)completedLessons / totalLessonsPossible * 100) : 0.0;

        stats.put("avgCompletion", avgComp);
        stats.put("avgQuizScore", avgScore);
        return stats;
    }

    // --- Helpers ---
    public Student getStudentById(String id) { return (Student) users.stream().filter(u -> u instanceof Student && u.getUserId().equals(id)).findFirst().orElse(null); }
    public Instructor getInstructorById(String id) { return (Instructor) users.stream().filter(u -> u instanceof Instructor && u.getUserId().equals(id)).findFirst().orElse(null); }
    public Course getCourseById(String id) { return courses.stream().filter(c -> c.getCourseId().equals(id)).findFirst().orElse(null); }
}