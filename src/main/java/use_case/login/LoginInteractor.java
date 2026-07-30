package use_case.login;

import entity.User;
import org.mindrot.jbcrypt.BCrypt;
import use_case.gateway.UserDataAccessInterface;

public final class LoginInteractor implements LoginInputBoundary {
    private final UserDataAccessInterface users;
    private final LoginOutputBoundary presenter;
    public LoginInteractor(UserDataAccessInterface users, LoginOutputBoundary presenter) {
        this.users = users; this.presenter = presenter;
    }
    @Override public void execute(LoginInputData input) {
        User user = users.get(input.username()).orElse(null);
        if (user == null || !BCrypt.checkpw(input.password(), user.passwordHash())) {
            presenter.present(new LoginOutputData(false, null, "Incorrect username or password")); return;
        }
        users.setCurrentUser(user);
        presenter.present(new LoginOutputData(true, user.username(), "Welcome back, " + user.username()));
    }
}
