package entity;

public record User(String username, String passwordHash) {
    public User {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("Password is required");
    }
}
