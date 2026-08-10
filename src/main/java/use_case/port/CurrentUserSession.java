package use_case.port;

import java.util.Optional;

import entity.User;

/**
 * Holds the authenticated user for the current application session.
 */
public interface CurrentUserSession {
    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    Optional<User> currentUser();

    /**
     * Performs this operation.
     *
     * @param user parameter value.
     */
    void setCurrentUser(User user);

    /**
     * Performs this operation.
     */
    void clear();
}
