package use_case.port;

import java.util.Optional;

import entity.User;

public interface UserRepository {
    /**
     * Performs this operation.
     *
     * @param username parameter value.
     *
     * @return the operation result.
     */
    Optional<User> get(String username);

    /**
     * Performs this operation.
     *
     * @param username parameter value.
     *
     * @return the operation result.
     */
    boolean existsByName(String username);

    /**
     * Performs this operation.
     *
     * @param user parameter value.
     */
    void save(User user);

    /**
     * Performs this operation.
     *
     * @param username parameter value.
     */
    void removeUser(String username);
}
