package interface_adapter.logout;

import use_case.login.LoginInputBoundary;
import use_case.login.LoginInputData;
import use_case.logout.LogoutInputBoundary;

public final class LogoutController {
    private final LogoutInputBoundary interactor;

    public LogoutController(LogoutInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute() {
        interactor.execute();
    }
}
