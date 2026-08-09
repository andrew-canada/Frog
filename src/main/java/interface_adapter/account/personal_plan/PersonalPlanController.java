package interface_adapter.account.personal_plan;

import use_case.account.personal_plan.PersonalPlanInputBoundary;
import use_case.account.personal_plan.PersonalPlanInputData;

public final class PersonalPlanController {

    private final PersonalPlanInputBoundary interactor;

    public PersonalPlanController(final PersonalPlanInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(final String calendarPath, final String nTrips, final String semester) {
        interactor.execute(new PersonalPlanInputData(calendarPath, nTrips, semester));
    }

}
