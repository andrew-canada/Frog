package interface_adapter.account.change_username;

import interface_adapter.account.AccountState;
import interface_adapter.account.AccountViewModel;
import use_case.account.change_username.ChangeUsernameOutputBoundary;
import use_case.account.change_username.ChangeUsernameOutputData;

public final class ChangeUsernamePresenter implements ChangeUsernameOutputBoundary {

    private final AccountViewModel viewModel;

    public ChangeUsernamePresenter(AccountViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void present(ChangeUsernameOutputData outputData) {

        System.out.println("in the presenter:" + outputData.success());
        AccountState state = viewModel.getState();
        state.setChangeUsernameMessage(outputData.message());
        state.setChangeUsernameSuccess(outputData.success());
        state.setUsername(outputData.username());

    }

}
