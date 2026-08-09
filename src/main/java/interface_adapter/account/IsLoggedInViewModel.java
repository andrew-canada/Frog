package interface_adapter.account;

import interface_adapter.common.ViewModel;

public final class IsLoggedInViewModel extends ViewModel<IsLoggedInState> {

    public IsLoggedInViewModel() {
        super(new IsLoggedInState(false, ""));
    }

}
