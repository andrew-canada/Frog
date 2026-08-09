package use_case.port;

import java.util.Optional;

import entity.User;

/**
 * Holds the authenticated user for the current application session.
 */
public interface CurrentUserSession {
    Optional<User> currentUser();

    void setCurrentUser(User user);

    void clear();
}
