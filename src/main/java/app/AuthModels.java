package app;

import interface_adapter.account.IsLoggedInViewModel;
import interface_adapter.login.LoggedInViewModel;
import interface_adapter.login.LoginViewModel;

/** Authentication state shared by authentication use cases and views. */
final class AuthModels {
    private final IsLoggedInViewModel isLoggedIn = new IsLoggedInViewModel();
    private final LoginViewModel login = new LoginViewModel();
    private final LoggedInViewModel loggedIn = new LoggedInViewModel();

    IsLoggedInViewModel isLoggedIn() {
        return isLoggedIn;
    }

    LoginViewModel login() {
        return login;
    }

    LoggedInViewModel loggedIn() {
        return loggedIn;
    }

    String username() {
        return loggedIn.getState().username();
    }

    boolean loggedInUser() {
        return loggedIn.getState().loggedIn();
    }
}
