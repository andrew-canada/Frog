package use_case.port;

import entity.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> get(String username);

    boolean existsByName(String username);

    void save(User user);

    void removeUser(String username);
}
