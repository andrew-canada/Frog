package interface_adapter.account.load_account;

import interface_adapter.account.AccountViewModel;
import use_case.account.load_account.LoadAccountOutputBoundary;
import use_case.account.load_account.LoadAccountOutputData;

public final class LoadAccountPresenter implements LoadAccountOutputBoundary {

    private final AccountViewModel viewModel;

    public LoadAccountPresenter(final AccountViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(final LoadAccountOutputData outputData) {

        viewModel.setUsername(outputData.username());
        viewModel.setPersonalPlan(outputData.personalPlan());

    }

}
