package data_access.user;

import entity.User;
import use_case.gateway.UserDataAccessInterface;
import use_case.login.Passwords;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryUserDataAccessObject implements UserDataAccessInterface {
    private final Map<String, User> users = new LinkedHashMap<>();
    private User currentUser;

    public InMemoryUserDataAccessObject() { save(new User("sheena_q", Passwords.hash("demo"))); }
    @Override public Optional<User> get(String username) { return Optional.ofNullable(users.get(username)); }
    @Override public boolean existsByName(String username) { return users.containsKey(username); }
    @Override public void save(User user) { users.put(user.username(), user); }
    @Override public void setCurrentUser(User user) { currentUser = user; }
    @Override public Optional<User> getCurrentUser() { return Optional.ofNullable(currentUser); }
}
