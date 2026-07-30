package interface_adapter.account.change_password;

import use_case.account.change_password.ChangePasswordInputBoundary;
import use_case.account.change_password.ChangePasswordInputData;

public final class ChangePasswordController {

    private final ChangePasswordInputBoundary interactor;

    public ChangePasswordController(ChangePasswordInputBoundary interactor) {
        this.interactor=interactor;
    }

    public void execute(String newPassword, String confirmNewPassword) {
        interactor.execute(new ChangePasswordInputData(newPassword, confirmNewPassword));
    }

}