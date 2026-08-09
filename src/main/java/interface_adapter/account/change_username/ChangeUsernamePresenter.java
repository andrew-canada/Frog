package interface_adapter.account.change_username;

import interface_adapter.account.AccountState;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.IsLoggedInState;
import interface_adapter.account.IsLoggedInViewModel;
import use_case.account.change_username.ChangeUsernameOutputBoundary;
import use_case.account.change_username.ChangeUsernameOutputData;

public final class ChangeUsernamePresenter implements ChangeUsernameOutputBoundary {

    private final AccountViewModel viewModel;
    private final IsLoggedInViewModel isLoggedInViewModel;

    public ChangeUsernamePresenter(final AccountViewModel viewModel, final IsLoggedInViewModel isLoggedInViewModel) {
        this.viewModel = viewModel;
        this.isLoggedInViewModel = isLoggedInViewModel;
    }

    @Override
    public void present(final ChangeUsernameOutputData outputData) {

        final AccountState state = viewModel.getState();
        state.setChangeUsernameMessage(outputData.message());
        state.setChangeUsernameSuccess(outputData.success());
        state.setUsername(outputData.username());
        final IsLoggedInState loggedInState = isLoggedInViewModel.getState();
        loggedInState.setUsername(outputData.username());

    }

}
