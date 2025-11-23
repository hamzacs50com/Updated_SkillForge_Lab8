package models;

public abstract class User {
    protected String userId;
    protected String username;
    protected String email;
    protected String passwordHash;
    protected String role;

    public User(String userId, String username, String email, String passwordHash, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }

    @Override
    public String toString() {
        return username + " (" + email + ")";
    }
}