package use_case.port;

import entity.User;

import java.util.Optional;

/** Holds the authenticated user for the current application session. */
public interface CurrentUserSession {
    Optional<User> currentUser();

    void setCurrentUser(User user);

    void clear();
}
