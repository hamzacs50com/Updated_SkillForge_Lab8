package models;

public abstract class User {

    protected String userId;
    protected String username;
    protected String email;
    protected String passwordHash;
    protected String role; // "Student" or "Instructor"

    public User(String userId, String username, String email, String passwordHash, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    
    @Override
    public String toString() {
        // This tells the JList to display the username and email
        return this.username + " (" + this.email + ")";
    }
}