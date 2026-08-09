package interface_adapter.account.delete_account;

import interface_adapter.account.AccountState;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.IsLoggedInState;
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

        final AccountState state = viewModel.getState();
        state.setDeleteAccountSuccess(outputData.success());
        state.setDeleteAccountMessage(outputData.message());
        final IsLoggedInState loggedInState = isLoggedInViewModel.getState();
        if (outputData.success()) {
            loggedInState.setUsername("");
            loggedInState.setIsLoggedIn(false);
        }

    }

}
