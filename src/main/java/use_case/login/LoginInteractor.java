package use_case.login;

import entity.User;
import org.mindrot.jbcrypt.BCrypt;
import use_case.gateway.UserDataAccessInterface;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class LoginInteractor implements LoginInputBoundary {
    private final UserDataAccessInterface users;
    private final LoginOutputBoundary presenter;

    public LoginInteractor(UserDataAccessInterface users, LoginOutputBoundary presenter) {
        this.users = users;
        this.presenter = presenter;
    }

    @Override
    public void execute(LoginInputData input) {
        User user = users.get(input.username()).orElse(null);
        if (user == null || !passwordMatches(input.password(), user.passwordHash())) {
            presenter.present(new LoginOutputData(false, null, "Incorrect username or password"));
            return;
        }
        if (!isBcryptHash(user.passwordHash())) {
            user = new User(user.username(), BCrypt.hashpw(input.password(), BCrypt.gensalt()), user.personalPlan());
            users.save(user);
        }
        users.setCurrentUser(user);
        presenter.present(new LoginOutputData(true, user.username(), "Welcome back, " + user.username()));
    }

    private static boolean passwordMatches(String password, String stored) {
        if (password == null || stored == null || stored.isBlank()) return false;
        if (isBcryptHash(stored)) {
            try {
                return BCrypt.checkpw(password, stored);
            } catch (IllegalArgumentException malformedHash) {
                return false;
            }
        }
        if (stored.matches("\\d+:.*")) return Passwords.matches(password, stored);
        return MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8), stored.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isBcryptHash(String value) {
        return value.startsWith("$2");
    }
}
