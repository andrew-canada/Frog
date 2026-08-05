package interface_adapter.login;

import use_case.signup.SignupOutputBoundary;
import use_case.signup.SignupOutputData;

public final class SignupPresenter implements SignupOutputBoundary {
    private final LoginViewModel login;
    private final LoggedInViewModel loggedIn;

    public SignupPresenter(LoginViewModel login, LoggedInViewModel loggedIn) {
        this.login = login;
        this.loggedIn = loggedIn;
    }

    @Override
    public void present(SignupOutputData d) {
        login.setState(new LoginViewModel.State(d.success(), d.username(), d.message()));
        if (d.success()) loggedIn.setState(new LoggedInViewModel.State(true, d.username()));
    }
}
