package use_case.logout;

import entity.User;
import data_access.user.UserDataAccessInterface;

public final class LogoutInteractor implements LogoutInputBoundary {

    private final UserDataAccessInterface users;
    private final LogoutOutputBoundary presenter;

    public LogoutInteractor(UserDataAccessInterface users, LogoutOutputBoundary presenter) {
        this.users = users;
        this.presenter = presenter;
    }

    @Override
    public void execute() {

        users.setCurrentUser(null);
        presenter.present();

    }

}
