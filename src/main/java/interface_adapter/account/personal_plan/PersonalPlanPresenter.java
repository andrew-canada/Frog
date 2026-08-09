package interface_adapter.account.personal_plan;

import interface_adapter.account.AccountState;
import interface_adapter.account.AccountViewModel;
import use_case.account.personal_plan.PersonalPlanOutputBoundary;
import use_case.account.personal_plan.PersonalPlanOutputData;

public final class PersonalPlanPresenter implements PersonalPlanOutputBoundary {

    private final AccountViewModel viewModel;

    public PersonalPlanPresenter(final AccountViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(final PersonalPlanOutputData outputData) {

        final AccountState state = viewModel.getState();
        state.setPersonalPlanSuccess(outputData.success());
        state.setPersonalPlan(outputData.personalPlan());
        state.setPersonalPlanMessage(outputData.message());

    }

}
