package interface_adapter.account;

import interface_adapter.common.ViewModel;

public final class AccountViewModel extends ViewModel<AccountState> {

    public AccountViewModel() {
        super(new AccountState("", "", true, "", true, "", true, true, ""));
    }

}