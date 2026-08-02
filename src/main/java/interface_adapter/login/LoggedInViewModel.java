package interface_adapter.login;

import interface_adapter.common.ViewModel;

public final class LoggedInViewModel extends ViewModel<LoggedInViewModel.State> {
    public LoggedInViewModel() {
        super(new State(false, "Guest"));
    }

    public record State(boolean loggedIn, String username) {
    }
}
