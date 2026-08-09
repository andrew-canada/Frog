package use_case.signup;

import entity.User;
import use_case.port.CurrentUserSession;
import use_case.port.PasswordHasher;
import use_case.port.UserRepository;

public final class SignupInteractor implements SignupInputBoundary {
    private final UserRepository users;
    private final PasswordHasher passwords;
    private final CurrentUserSession session;
    private final SignupOutputBoundary presenter;

    public SignupInteractor(final UserRepository users, final CurrentUserSession session,
                            final PasswordHasher passwords, final SignupOutputBoundary presenter) {
        this.users = users;
        this.session = session;
        this.passwords = passwords;
        this.presenter = presenter;
    }

    @Override
    public void execute(final SignupInputData input) {
        final String name = input.username() == null ? "" : input
                                                            .username()
                                                            .trim();
        if (name.length() < 3 || input.password() == null || input
            .password()
            .length() < 4) {
            presenter.present(
                new SignupOutputData(false, null, "Use 3+ characters for the name and 4+ for the password"));
            return;
        }
        if (users.existsByName(name)) {
            presenter.present(new SignupOutputData(false, null, "That username is already taken"));
            return;
        }
        final User user = new User(name, passwords.hash(input.password()), "");
        users.save(user);
        session.setCurrentUser(user);
        presenter.present(new SignupOutputData(true, name, "Account created for " + name));
    }
}
