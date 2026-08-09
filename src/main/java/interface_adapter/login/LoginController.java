package interface_adapter.login;

import use_case.login.LoginInputBoundary;
import use_case.login.LoginInputData;

public final class LoginController {
    private final LoginInputBoundary interactor;

    public LoginController(final LoginInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(final String username, final String password) {
        interactor.execute(new LoginInputData(username, password));
    }
}
