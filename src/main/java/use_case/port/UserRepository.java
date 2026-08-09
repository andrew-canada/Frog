package use_case.port;

import java.util.Optional;

import entity.User;

public interface UserRepository {
    Optional<User> get(String username);

    boolean existsByName(String username);

    void save(User user);

    void removeUser(String username);
}
