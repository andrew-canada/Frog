package use_case.signup;

import entity.User;
import use_case.port.CurrentUserSession;
import use_case.port.PasswordHasher;
import use_case.port.UserRepository;

public final class SignupInteractor implements SignupInputBoundary {
    private static final int MINIMUM_PASSWORD_LENGTH = 4;
    private static final int MINIMUM_USERNAME_LENGTH = 3;
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
        final String name;
        if (input.username() == null) {
            name = "";
        }
        else {
            name = input
                .username()
                .trim();
        }
        if (name.length() < MINIMUM_USERNAME_LENGTH || input.password() == null || input
            .password()
            .length() < MINIMUM_PASSWORD_LENGTH) {
            presenter.present(
                new SignupOutputData(false, null, "Use 3+ characters for the name and 4+ for the password"));
        }
        else if (users.existsByName(name)) {
            presenter.present(new SignupOutputData(false, null, "That username is already taken"));
        }
        else {
            final User user = new User(name, passwords.hash(input.password()), "");
            users.save(user);
            session.setCurrentUser(user);
            presenter.present(new SignupOutputData(true, name, "Account created for " + name));
        }
    }
}
