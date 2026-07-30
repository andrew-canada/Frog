package interface_adapter.account.delete_account;

import interface_adapter.account.AccountState;
import interface_adapter.account.AccountViewModel;
import use_case.account.delete_account.DeleteAccountOutputBoundary;
import use_case.account.delete_account.DeleteAccountOutputData;

public final class DeleteAccountPresenter implements DeleteAccountOutputBoundary {

    private final AccountViewModel viewModel;

    public DeleteAccountPresenter(AccountViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void present(DeleteAccountOutputData outputData) {

        AccountState state = viewModel.getState();
        state.setDeleteAccountSuccess(outputData.success());

    }

}
