package entity;

public record User(String username, String passwordHash, String personalPlan,
                   boolean moderator) implements entity.user.User {
    public User {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("Password is required");
    }

    /**
     * Convenience constructor for an ordinary (non-moderator) user.
     */
    public User(String username, String passwordHash, String personalPlan) {
        this(username, passwordHash, personalPlan, false);
    }

    @Override
    public String name() {
        return username;
    }

    /**
     * @return whether this user has moderator privileges.
     */
    public boolean isModerator() {
        return moderator;
    }
}
