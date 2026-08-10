package interface_adapter.account.personal_plan;

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

        viewModel.setPersonalPlanSuccess(outputData.success());
        viewModel.setPersonalPlanMessage(outputData.message());
        if (outputData.success()) {
            viewModel.setPersonalPlan(outputData.personalPlan());
        }

    }

}
