package interface_adapter.account.delete_account;

import interface_adapter.account.AccountViewModel;
import interface_adapter.account.IsLoggedInViewModel;
import use_case.account.delete_account.DeleteAccountOutputBoundary;
import use_case.account.delete_account.DeleteAccountOutputData;

public final class DeleteAccountPresenter implements DeleteAccountOutputBoundary {

    private final AccountViewModel viewModel;
    private final IsLoggedInViewModel isLoggedInViewModel;

    public DeleteAccountPresenter(final AccountViewModel viewModel, final IsLoggedInViewModel isLoggedInViewModel) {
        this.viewModel = viewModel;
        this.isLoggedInViewModel = isLoggedInViewModel;
    }

    @Override
    public void present(final DeleteAccountOutputData outputData) {

        viewModel.setDeleteAccountSuccess(outputData.success());
        viewModel.setDeleteAccountMessage(outputData.message());
        if (outputData.success()) {
            isLoggedInViewModel.setUsername("");
            isLoggedInViewModel.setIsLoggedIn(false);
        }

    }

}
