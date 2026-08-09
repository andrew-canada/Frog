package data_access.user;

import entity.User;
import org.mindrot.jbcrypt.BCrypt;
import use_case.moderate_reviews.ModeratorDataAccessInterface;
import use_case.port.UserRepository;
import use_case.port.CurrentUserSession;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryUserDataAccessObject
        implements UserRepository, CurrentUserSession, ModeratorDataAccessInterface {
    private final Map<String, User> users = new LinkedHashMap<>();
    private User currentUser;

    public InMemoryUserDataAccessObject() {
        save(new User("sheena_q", BCrypt.hashpw("demo", BCrypt.gensalt()), ""));
    }

    @Override
    public Optional<User> get(String username) {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public boolean existsByName(String username) {
        return users.containsKey(username);
    }

    @Override
    public void save(User user) {
        users.put(user.username(), user);
    }

    @Override
    public Optional<User> currentUser() {
        return Optional.ofNullable(currentUser);
    }

    @Override
    public void setCurrentUser(User user) {
        currentUser = user;
    }

    @Override
    public void clear() {
        currentUser = null;
    }

    @Override
    public void removeUser(String username) {
        users.remove(username);
    }

    @Override
    public boolean isModerator(String username) {
        return get(username).map(User::isModerator).orElse(false);
    }
}
