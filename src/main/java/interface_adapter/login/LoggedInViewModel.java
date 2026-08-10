package interface_adapter.login;

import interface_adapter.common.AbstractViewModel;

public final class LoggedInViewModel extends AbstractViewModel<LoggedInViewModel.State> {
    public LoggedInViewModel() {
        super(new State(false, "Guest", false));
    }

    public record State(boolean loggedIn, String username, boolean moderator) {
    }
}
