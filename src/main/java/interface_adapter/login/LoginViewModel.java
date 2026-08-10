package interface_adapter.login;

import interface_adapter.common.AbstractViewModel;

public final class LoginViewModel extends AbstractViewModel<LoginViewModel.State> {
    public LoginViewModel() {
        super(new State(false, "", ""));
    }

    public record State(boolean success, String username, String message) {
    }
}
