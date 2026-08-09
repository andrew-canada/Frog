package use_case.account.change_password;

import use_case.port.UserRepository;
import use_case.port.PasswordHasher;
import use_case.port.CurrentUserSession;
import entity.User;

public final class ChangePasswordInteractor implements ChangePasswordInputBoundary {

    private final UserRepository users;
    private final PasswordHasher passwords;
    private final CurrentUserSession session;
    private final ChangePasswordOutputBoundary presenter;

    public ChangePasswordInteractor(UserRepository users, CurrentUserSession session, PasswordHasher passwords,
                                    ChangePasswordOutputBoundary presenter) {
        this.users = users;
        this.session = session;
        this.passwords = passwords;
        this.presenter = presenter;
    }

    @Override
    public void execute(ChangePasswordInputData input) {

        User user = session.currentUser().orElse(null);

        if (user == null) {
            presenter.present(new ChangePasswordOutputData(false, "Not logged in"));
        } else if (!input.newPassword().equals(input.confirmNewPassword())) {
            presenter.present(new ChangePasswordOutputData(false, "Passwords do not match"));
        } else if (input.newPassword().length() < 4) {
            presenter.present(new ChangePasswordOutputData(false, "Password needs 4+ characters"));
        } else {
            users.removeUser(user.username());
            User newUser = new User(user.username(), passwords.hash(input.newPassword()), user.personalPlan());
            users.save(newUser);
            session.setCurrentUser(newUser);
            presenter.present(new ChangePasswordOutputData(true, "Changed password"));
        }

    }

}
