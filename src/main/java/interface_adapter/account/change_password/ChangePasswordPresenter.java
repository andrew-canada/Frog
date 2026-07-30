package interface_adapter.account.change_password;

import interface_adapter.account.AccountState;
import interface_adapter.account.AccountViewModel;
import use_case.account.change_password.ChangePasswordOutputBoundary;
import use_case.account.change_password.ChangePasswordOutputData;

public final class ChangePasswordPresenter implements ChangePasswordOutputBoundary {

    private final AccountViewModel viewModel;

    public ChangePasswordPresenter(AccountViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void present(ChangePasswordOutputData outputData) {

        AccountState state = viewModel.getState();
        state.setChangePasswordMessage(outputData.message());
        state.setChangePasswordSuccess(outputData.success());

    }

}