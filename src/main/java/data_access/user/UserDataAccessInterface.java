package data_access.user;

import entity.User;

import java.util.Optional;

public interface UserDataAccessInterface {
    Optional<User> get(String username);

    boolean existsByName(String username);

    void save(User user);

    void setCurrentUser(User user);

    Optional<User> getCurrentUser();

    void removeUser(String username);
}
