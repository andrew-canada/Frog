package interface_adapter.account;

import interface_adapter.common.AbstractViewModel;

public final class IsLoggedInViewModel extends AbstractViewModel<IsLoggedInState> {

    public IsLoggedInViewModel() {
        super(new IsLoggedInState(false, ""));
    }

}
