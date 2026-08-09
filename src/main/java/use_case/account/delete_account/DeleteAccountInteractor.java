package use_case.account.delete_account;

import data_access.user.UserDataAccessInterface;
import entity.User;

public final class DeleteAccountInteractor implements DeleteAccountInputBoundary {

    private final UserDataAccessInterface users;
    private final DeleteAccountOutputBoundary presenter;

    public DeleteAccountInteractor(UserDataAccessInterface users, DeleteAccountOutputBoundary presenter) {
        this.users = users;
        this.presenter = presenter;
    }

    @Override
    public void execute() {

        User user = users.getCurrentUser().orElse(null);

        if (user == null) {
            presenter.present(new DeleteAccountOutputData(false, "You are not logged in"));
        } else {
            users.removeUser(user.username());
            users.setCurrentUser(null);
            presenter.present(new DeleteAccountOutputData(true, ""));
        }

    }

}
