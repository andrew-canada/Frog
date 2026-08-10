package entity;

public record User(String username, String passwordHash, String personalPlan, boolean moderator) {
    public User {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    /**
     * Convenience constructor for an ordinary (non-moderator) user.
     */
    public User(final String username, final String passwordHash, final String personalPlan) {
        this(username, passwordHash, personalPlan, false);
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public String name() {
        return username;
    }

    /**
     * Checks whether this user has moderator privileges.
     *
     * @return whether this user has moderator privileges
     */
    public boolean isModerator() {
        return moderator;
    }
}
