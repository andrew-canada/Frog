package interface_adapter.account.change_username;

import interface_adapter.account.AccountViewModel;
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

        viewModel.setChangeUsernameMessage(outputData.message());
        viewModel.setChangeUsernameSuccess(outputData.success());
        viewModel.setUsername(outputData.username());
        isLoggedInViewModel.setUsername(outputData.username());

    }

}
