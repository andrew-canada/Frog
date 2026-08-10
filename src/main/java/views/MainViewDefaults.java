package views;

import database.user.InMemoryUserDataAccessObject;
import interface_adapter.account.IsLoggedInViewModel;
import interface_adapter.login.LoggedInViewModel;
import interface_adapter.login.LoginViewModel;
import interface_adapter.logout.LogoutController;
import interface_adapter.logout.LogoutPresenter;
import use_case.logout.LogoutInteractor;

final class MainViewDefaults {
    private MainViewDefaults() {
    }

    static LogoutController logoutController() {
        return new LogoutController(new LogoutInteractor(new InMemoryUserDataAccessObject(),
            new LogoutPresenter(new IsLoggedInViewModel(), new LoginViewModel(), new LoggedInViewModel())));
    }
}
