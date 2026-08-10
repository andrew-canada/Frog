package interface_adapter.account;

import interface_adapter.common.AbstractViewModel;

public final class AccountViewModel extends AbstractViewModel<AccountState> {

    public AccountViewModel() {
        super(new AccountState("", null, false, "", false, "", false, "", false, ""));
    }

}
