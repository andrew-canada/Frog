package use_case.login;

import entity.User;
import use_case.port.CurrentUserSession;
import use_case.port.PasswordHasher;
import use_case.port.UserRepository;

public final class LoginInteractor implements LoginInputBoundary {
    private final UserRepository users;
    private final PasswordHasher passwords;
    private final CurrentUserSession session;
    private final LoginOutputBoundary presenter;

    public LoginInteractor(final UserRepository users, final CurrentUserSession session, final PasswordHasher passwords,
                           final LoginOutputBoundary presenter) {
        this.users = users;
        this.session = session;
        this.passwords = passwords;
        this.presenter = presenter;
    }

    @Override
    public void execute(final LoginInputData input) {
        User user = users
            .get(input.username())
            .orElse(null);
        if (user == null || !passwords.matches(input.password(), user.passwordHash())) {
            presenter.present(new LoginOutputData(false, null, false, "Incorrect username or password"));
        }
        else {
            if (!passwords.isCurrentHash(user.passwordHash())) {
                user = new User(user.username(), passwords.hash(input.password()), user.personalPlan());
                users.save(user);
            }
            session.setCurrentUser(user);
            presenter.present(
                new LoginOutputData(true, user.username(), user.isModerator(), "Welcome back, " + user.username()));
        }
    }
}
