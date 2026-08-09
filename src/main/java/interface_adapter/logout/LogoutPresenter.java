package interface_adapter.logout;

import interface_adapter.account.IsLoggedInViewModel;
import use_case.logout.LogoutOutputBoundary;

public final class LogoutPresenter implements LogoutOutputBoundary {
    private final IsLoggedInViewModel isLoggedIn;

    public LogoutPresenter(IsLoggedInViewModel isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    @Override
    public void present() {
        isLoggedIn.getState().setIsLoggedIn(false);
        isLoggedIn.getState().setUsername("");
    }
}
