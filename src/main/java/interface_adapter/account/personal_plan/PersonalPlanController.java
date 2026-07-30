package interface_adapter.account.personal_plan;

import use_case.account.personal_plan.PersonalPlanInputBoundary;
import use_case.account.personal_plan.PersonalPlanInputData;

public final class PersonalPlanController {

    private final PersonalPlanInputBoundary interactor;

    public PersonalPlanController(PersonalPlanInputBoundary interactor) {
        this.interactor=interactor;
    }

    public void execute(String calendarPath, String nTrips) {
        interactor.execute(new PersonalPlanInputData(calendarPath, nTrips));
    }

}
