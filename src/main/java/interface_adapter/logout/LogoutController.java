package interface_adapter.logout;

import use_case.logout.LogoutInputBoundary;

public final class LogoutController {
    private final LogoutInputBoundary interactor;

    public LogoutController(final LogoutInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute() {
        interactor.execute();
    }
}
