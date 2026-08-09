package use_case.account.change_password;

import entity.User;
import use_case.port.CurrentUserSession;
import use_case.port.PasswordHasher;
import use_case.port.UserRepository;

public final class ChangePasswordInteractor implements ChangePasswordInputBoundary {

    private final UserRepository users;
    private final PasswordHasher passwords;
    private final CurrentUserSession session;
    private final ChangePasswordOutputBoundary presenter;

    public ChangePasswordInteractor(final UserRepository users, final CurrentUserSession session, final PasswordHasher passwords,
                                    final ChangePasswordOutputBoundary presenter) {
        this.users = users;
        this.session = session;
        this.passwords = passwords;
        this.presenter = presenter;
    }

    @Override
    public void execute(final ChangePasswordInputData input) {

        final User user = session
            .currentUser()
            .orElse(null);

        if (user == null) {
            presenter.present(new ChangePasswordOutputData(false, "Not logged in"));
        } else if (!input
            .newPassword()
            .equals(input.confirmNewPassword())) {
            presenter.present(new ChangePasswordOutputData(false, "Passwords do not match"));
        } else if (input
            .newPassword()
            .length() < 4) {
            presenter.present(new ChangePasswordOutputData(false, "Password needs 4+ characters"));
        } else {
            users.removeUser(user.username());
            final User newUser = new User(user.username(), passwords.hash(input.newPassword()), user.personalPlan());
            users.save(newUser);
            session.setCurrentUser(newUser);
            presenter.present(new ChangePasswordOutputData(true, "Changed password"));
        }

    }

}
