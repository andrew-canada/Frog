package interface_adapter.login;

import interface_adapter.common.ViewModel;

public final class LoginViewModel extends ViewModel<LoginViewModel.State> {
    public LoginViewModel() {
        super(new State(false, "", ""));
    }

    public record State(boolean success, String username, String message) {
    }
}
