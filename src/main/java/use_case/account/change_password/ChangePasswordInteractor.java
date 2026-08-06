package use_case.account.change_password;

import entity.User;
import data_access.user.UserDataAccessInterface;
import org.mindrot.jbcrypt.BCrypt;

public final class ChangePasswordInteractor implements ChangePasswordInputBoundary {

    private final UserDataAccessInterface users;
    private final ChangePasswordOutputBoundary presenter;

    public ChangePasswordInteractor(UserDataAccessInterface users, ChangePasswordOutputBoundary presenter) {
        this.users = users;
        this.presenter = presenter;
    }

    @Override
    public void execute(ChangePasswordInputData input) {

        User user = users.getCurrentUser().orElse(null);

        if (user == null) {
            presenter.present(new ChangePasswordOutputData(false, "Not logged in"));
        } else if (!input.newPassword().equals(input.confirmNewPassword())) {
            presenter.present(new ChangePasswordOutputData(false, "Passwords do not match"));
        } else if (input.newPassword().length() < 4) {
            presenter.present(new ChangePasswordOutputData(false, "Password needs 4+ characters"));
        } else {
            users.removeUser(user.username());
            User newUser = new User(user.username(), BCrypt.hashpw(input.newPassword(), BCrypt.gensalt()), user.personalPlan());
            users.save(newUser);
            users.setCurrentUser(newUser);
            presenter.present(new ChangePasswordOutputData(true, "Changed password"));
        }

    }

}
