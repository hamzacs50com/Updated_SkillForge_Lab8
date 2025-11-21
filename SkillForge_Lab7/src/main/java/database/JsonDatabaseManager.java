package database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import models.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages all database read/write operations for users.json and courses.json.
 * This is the "API" for the frontend team.
 */
public class JsonDatabaseManager {

    // --- Singleton Pattern Setup ---
    private static JsonDatabaseManager instance;

    // File paths
    private static final String USERS_FILE = "data/users.json";
    private static final String COURSES_FILE = "data/courses.json";

    // In-memory data
    private List<User> users;
    private List<Course> courses;
    private Gson gson;

    // Private constructor
    private JsonDatabaseManager() {
        // Use a custom builder to handle the abstract 'User' class
        // This tells Gson: when you see a 'User' class, it might be 'Student' or 'Instructor'
        // This is an advanced (but necessary) setup for polymorphism.
        // A simpler way is to store Students and Instructors in separate lists,
        // but this approach is cleaner for the 'users.json' file.
        // For this project, a simpler model is better. Let's store two separate lists.
        
        gson = new GsonBuilder().setPrettyPrinting().create();
        
        // We will store Students and Instructors in the same 'users' list
        // and handle the type during deserialization.
        // A simpler approach for this lab is to have one list of 'User'
        // and not use Gson for polymorphism, but rather handle it in our logic.
        // Let's stick to the simplest plan: read a generic list and cast it.
        
        // Simpler approach: Store all users as a List<Map<String, Object>>
        // and parse them into Student/Instructor objects manually.
        
        // Let's use an even simpler, more robust approach for this lab:
        // Two separate lists in the database manager.
        // (This is a common and practical solution)
        
        // This is a complex problem. Let's choose the most direct solution for the lab.
        // We'll store Students and Instructors in *separate files* or handle
        // the type manually. Let's stick to ONE users.json file and parse manually.
        
        loadUsers();
        loadCourses();
    }

    // Public method to get the single instance
    public static synchronized JsonDatabaseManager getInstance() {
        if (instance == null) {
            instance = new JsonDatabaseManager();
        }
        return instance;
    }

    // --- Data Loading and Saving ---

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        users = new ArrayList<>();
        try (FileReader reader = new FileReader(USERS_FILE)) {
            // Read as a list of generic maps
            Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> userMaps = gson.fromJson(reader, type);

            if (userMaps == null) {
                users = new ArrayList<>();
                return;
            }

            // Manually parse each user map based on its 'role'
            for (Map<String, Object> userMap : userMaps) {
                String role = (String) userMap.get("role");
                if ("Student".equals(role)) {
                    users.add(gson.fromJson(gson.toJson(userMap), Student.class));
                } else if ("Instructor".equals(role)) {
                    users.add(gson.fromJson(gson.toJson(userMap), Instructor.class));
                }
            }
        } catch (IOException e) {
            System.out.println("users.json not found, creating new list.");
            users = new ArrayList<>();
        }
    }

    private void saveUsers() {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCourses() {
        try (FileReader reader = new FileReader(COURSES_FILE)) {
            Type type = new TypeToken<List<Course>>() {}.getType();
            courses = gson.fromJson(reader, type);
            if (courses == null) {
                courses = new ArrayList<>();
            }
        } catch (IOException e) {
            System.out.println("courses.json not found, creating new list.");
            courses = new ArrayList<>();
        }
    }

    private void saveCourses() {
        try (FileWriter writer = new FileWriter(COURSES_FILE)) {
            gson.toJson(courses, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- User/Auth Methods ---

    public User loginUser(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                if (PasswordHasher.checkPassword(password, user.getPasswordHash())) {
                    return user; // Success
                }
            }
        }
        return null; // Failure
    }

    public boolean registerUser(String username, String email, String password, String role) {
        // 1. Check if email exists
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return false; // Email already in use
            }
        }

        // 2. Create new user
        String userId = UUID.randomUUID().toString();
        String passwordHash = PasswordHasher.hashPassword(password);
        User newUser;

        if ("Student".equals(role)) {
            newUser = new Student(userId, username, email, passwordHash, role, new ArrayList<>(), new java.util.HashMap<>());
        } else { // "Instructor"
            newUser = new Instructor(userId, username, email, passwordHash, role, new ArrayList<>());
        }

        // 3. Add and save
        users.add(newUser);
        saveUsers();
        return true;
    }

    // --- Student-Facing Methods ---

    public List<Course> getAllCourses() {
        return courses;
    }

    public List<Course> getEnrolledCourses(String studentId) {
        Student student = getStudentById(studentId);
        if (student == null) {
            return new ArrayList<>();
        }
        
        List<String> enrolledCourseIds = student.getEnrolledCourses();
        return courses.stream()
                .filter(course -> enrolledCourseIds.contains(course.getCourseId()))
                .collect(Collectors.toList());
    }

    public void enrollStudentInCourse(String studentId, String courseId) {
        Student student = getStudentById(studentId);
        Course course = getCourseById(courseId);

        if (student != null && course != null) {
            // Add course to student
            if (!student.getEnrolledCourses().contains(courseId)) {
                student.getEnrolledCourses().add(courseId);
                // Initialize progress for this new course
                student.getProgress().putIfAbsent(courseId, new ArrayList<>());
            }
            
            // Add student to course
            if (!course.getStudents().contains(studentId)) {
                course.getStudents().add(studentId);
            }
            
            saveUsers();
            saveCourses();
        }
    }

    public void markLessonAsCompleted(String studentId, String courseId, String lessonId) {
        Student student = getStudentById(studentId);
        if (student != null) {
            Map<String, List<String>> progress = student.getProgress();
            // Ensure the course progress list exists
            progress.putIfAbsent(courseId, new ArrayList<>());
            // Add lesson if not already completed
            if (!progress.get(courseId).contains(lessonId)) {
                progress.get(courseId).add(lessonId);
                saveUsers();
            }
        }
    }
    
    // --- Instructor-Facing Methods ---

    public List<Course> getCoursesByInstructor(String instructorId) {
        return courses.stream()
                .filter(course -> course.getInstructorId().equals(instructorId))
                .collect(Collectors.toList());
    }

    public List<Student> getEnrolledStudents(String courseId) {
        Course course = getCourseById(courseId);
        if (course == null) {
            return new ArrayList<>();
        }
        
        List<String> studentIds = course.getStudents();
        return users.stream()
                .filter(user -> user instanceof Student && studentIds.contains(user.getUserId()))
                .map(user -> (Student) user)
                .collect(Collectors.toList());
    }

    public void createCourse(String title, String description, String instructorId) {
        String courseId = UUID.randomUUID().toString();
        Course newCourse = new Course(courseId, title, description, instructorId, new ArrayList<>(), new ArrayList<>());
        
        courses.add(newCourse);
        
        // Add to instructor's list
        Instructor instructor = getInstructorById(instructorId);
        if (instructor != null) {
            instructor.getCreatedCourses().add(courseId);
            saveUsers();
        }
        
        saveCourses();
    }
    
    public void addLesson(String courseId, String lessonTitle, String lessonContent) {
        Course course = getCourseById(courseId);
        if (course != null) {
            String lessonId = UUID.randomUUID().toString();
            Lesson newLesson = new Lesson(lessonId, lessonTitle, lessonContent, new ArrayList<>());
            course.getLessons().add(newLesson);
            saveCourses();
        }
    }
    
    public void deleteCourse(String courseId) {
        // Remove from main course list
        courses.removeIf(course -> course.getCourseId().equals(courseId));

        // Remove from all users (students and instructors)
        for (User user : users) {
            if (user instanceof Instructor) {
                ((Instructor) user).getCreatedCourses().remove(courseId);
            } else if (user instanceof Student) {
                ((Student) user).getEnrolledCourses().remove(courseId);
                // Also remove any progress for that course
                ((Student) user).getProgress().remove(courseId);
            }
        }
        
        saveCourses();
        saveUsers();
    }
    
    public void updateCourse(String courseId, String newTitle, String newDescription) {
        Course course = getCourseById(courseId);
        if (course != null) {
            course.setTitle(newTitle);
            course.setDescription(newDescription);
            saveCourses();
        }
    }
    
    public void deleteLesson(String courseId, String lessonId) {
        Course course = getCourseById(courseId);
        if (course != null) {
            // Remove the lesson from the course's lesson list
            course.getLessons().removeIf(lesson -> lesson.getLessonId().equals(lessonId));
            
            // Remove the lesson from all enrolled students' progress
            for (String studentId : course.getStudents()) {
                Student student = getStudentById(studentId);
                if (student != null && student.getProgress().containsKey(courseId)) {
                    student.getProgress().get(courseId).remove(lessonId);
                }
            }
            
            saveCourses();
            saveUsers();
        }
    }
    
    public void updateLesson(String courseId, String lessonId, String newTitle, String newContent) {
        Course course = getCourseById(courseId);
        if (course != null) {
            for (Lesson lesson : course.getLessons()) {
                if (lesson.getLessonId().equals(lessonId)) {
                    lesson.setTitle(newTitle);
                    lesson.setContent(newContent);
                    saveCourses();
                    return; // Lesson found and updated
                }
            }
        }
    }

    // ... (You would add updateCourse, deleteCourse, deleteLesson, etc. here)

    // --- Helper Methods ---
    
    public Student getStudentById(String studentId) {
        return users.stream()
                .filter(user -> user instanceof Student && user.getUserId().equals(studentId))
                .map(user -> (Student) user)
                .findFirst()
                .orElse(null);
    }
    
    public Instructor getInstructorById(String instructorId) {
        return users.stream()
                .filter(user -> user instanceof Instructor && user.getUserId().equals(instructorId))
                .map(user -> (Instructor) user)
                .findFirst()
                .orElse(null);
    }
    
    public Course getCourseById(String courseId) {
        return courses.stream()
                .filter(course -> course.getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);
    }
}
