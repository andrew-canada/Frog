package interface_adapter.login;

import use_case.login.LoginOutputBoundary;
import use_case.login.LoginOutputData;

public final class LoginPresenter implements LoginOutputBoundary {
    private final LoginViewModel login; private final LoggedInViewModel loggedIn;
    public LoginPresenter(LoginViewModel login,LoggedInViewModel loggedIn){this.login=login;this.loggedIn=loggedIn;}
    @Override public void present(LoginOutputData d){login.setState(new LoginViewModel.State(d.success(),d.username(),d.message()));
        if(d.success()) loggedIn.setState(new LoggedInViewModel.State(true,d.username()));}
}
