package interface_adapter.login;

import interface_adapter.account.IsLoggedInViewModel;
import use_case.login.LoginOutputBoundary;
import use_case.login.LoginOutputData;

public final class LoginPresenter implements LoginOutputBoundary {
    private final LoginViewModel login;
    private final LoggedInViewModel loggedIn;
    private final IsLoggedInViewModel isLoggedIn;

    public LoginPresenter(LoginViewModel login, LoggedInViewModel loggedIn, IsLoggedInViewModel isLoggedIn) {
        this.login = login;
        this.loggedIn = loggedIn;
        this.isLoggedIn = isLoggedIn;
    }

    @Override
    public void present(LoginOutputData d) {
        login.setState(new LoginViewModel.State(d.success(), d.username(), d.message()));
        if (d.success()) loggedIn.setState(new LoggedInViewModel.State(true, d.username(), d.moderator()));
        if (d.success()) {
            isLoggedIn.getState().setIsLoggedIn(true);
            isLoggedIn.getState().setUsername(d.username());
        }
    }
}
