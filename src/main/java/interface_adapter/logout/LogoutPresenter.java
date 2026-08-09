package interface_adapter.logout;

import interface_adapter.account.IsLoggedInViewModel;
import interface_adapter.login.LoggedInViewModel;
import interface_adapter.login.LoginViewModel;
import use_case.logout.LogoutOutputBoundary;

public final class LogoutPresenter implements LogoutOutputBoundary {
    private final IsLoggedInViewModel isLoggedIn;
    private final LoginViewModel login;
    private final LoggedInViewModel loggedIn;

    public LogoutPresenter(IsLoggedInViewModel isLoggedIn, LoginViewModel login, LoggedInViewModel loggedIn) {
        this.isLoggedIn = isLoggedIn;
        this.login = login;
        this.loggedIn = loggedIn;
    }

    @Override
    public void present() {
        isLoggedIn.getState().setIsLoggedIn(false);
        isLoggedIn.getState().setUsername("");
        loggedIn.setState(new LoggedInViewModel.State(false, "Guest", false));
        login.setState(new LoginViewModel.State(false, "", ""));
    }
}
