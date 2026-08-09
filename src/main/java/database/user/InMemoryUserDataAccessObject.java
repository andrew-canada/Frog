package database.user;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import entity.User;
import use_case.moderate_reviews.ModeratorDataAccessInterface;
import use_case.port.CurrentUserSession;
import use_case.port.UserRepository;

public final class InMemoryUserDataAccessObject
    implements UserRepository, CurrentUserSession, ModeratorDataAccessInterface {
    private final Map<String, User> users = new LinkedHashMap<>();
    private User currentUser;

    public InMemoryUserDataAccessObject() {
        save(new User("sheena_q", BCrypt.hashpw("demo", BCrypt.gensalt()), ""));
    }

    @Override
    public Optional<User> get(final String username) {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public boolean existsByName(final String username) {
        return users.containsKey(username);
    }

    @Override
    public void save(final User user) {
        users.put(user.username(), user);
    }

    @Override
    public Optional<User> currentUser() {
        return Optional.ofNullable(currentUser);
    }

    @Override
    public void setCurrentUser(final User user) {
        currentUser = user;
    }

    @Override
    public void clear() {
        currentUser = null;
    }

    @Override
    public void removeUser(final String username) {
        users.remove(username);
    }

    @Override
    public boolean isModerator(final String username) {
        return get(username)
            .map(User::isModerator)
            .orElse(false);
    }
}
