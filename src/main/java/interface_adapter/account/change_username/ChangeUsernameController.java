package interface_adapter.account.change_username;

import use_case.account.change_username.ChangeUsernameInputBoundary;
import use_case.account.change_username.ChangeUsernameInputData;

public final class ChangeUsernameController {

    private final ChangeUsernameInputBoundary interactor;

    public ChangeUsernameController(ChangeUsernameInputBoundary interactor) {
        this.interactor=interactor;
    }

    public void execute(String newUsername) {
        interactor.execute(new ChangeUsernameInputData(newUsername));
    }

}
