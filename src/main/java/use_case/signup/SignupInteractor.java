package use_case.signup;

import entity.User;
import use_case.gateway.UserDataAccessInterface;
import use_case.login.Passwords;

public final class SignupInteractor implements SignupInputBoundary {
    private final UserDataAccessInterface users;
    private final SignupOutputBoundary presenter;
    public SignupInteractor(UserDataAccessInterface users, SignupOutputBoundary presenter) {
        this.users = users; this.presenter = presenter;
    }
    @Override public void execute(SignupInputData input) {
        String name = input.username() == null ? "" : input.username().trim();
        if (name.length() < 3 || input.password() == null || input.password().length() < 4) {
            presenter.present(new SignupOutputData(false, null, "Use 3+ characters for the name and 4+ for the password")); return;
        }
        if (users.existsByName(name)) {
            presenter.present(new SignupOutputData(false, null, "That username is already taken")); return;
        }
        User user = new User(name, Passwords.hash(input.password()));
        users.save(user); users.setCurrentUser(user);
        presenter.present(new SignupOutputData(true, name, "Account created for " + name));
    }
}
