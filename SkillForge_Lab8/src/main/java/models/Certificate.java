package models;

public class Certificate {
    private String certificateId;
    private String studentId;
    private String studentName;
    private String courseId;
    private String courseTitle;
    private String issueDate;

    public Certificate(String certificateId, String studentId, String studentName, String courseId, String courseTitle, String issueDate) {
        this.certificateId = certificateId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.issueDate = issueDate;
    }
    
    public String getCourseId() { return courseId; }
    
    @Override
    public String toString() {
        return courseTitle + " (Issued: " + issueDate + ")";
    }
}