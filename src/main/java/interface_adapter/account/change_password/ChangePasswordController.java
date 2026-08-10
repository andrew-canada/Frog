package interface_adapter.account.change_password;

import use_case.account.change_password.ChangePasswordInputBoundary;
import use_case.account.change_password.ChangePasswordInputData;

public final class ChangePasswordController {

    private final ChangePasswordInputBoundary interactor;

    public ChangePasswordController(final ChangePasswordInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Performs this operation.
     *
     * @param newPassword parameter value.
     *
     * @param confirmNewPassword parameter value.
     */
    public void execute(final String newPassword, final String confirmNewPassword) {
        interactor.execute(new ChangePasswordInputData(newPassword, confirmNewPassword));
    }

}
