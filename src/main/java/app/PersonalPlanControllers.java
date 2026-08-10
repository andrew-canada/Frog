package app;

import database.personal_plan.FileCalendarContentReader;
import database.personal_plan.GeminiPersonalPlanGenerator;
import interface_adapter.account.personal_plan.PersonalPlanController;
import interface_adapter.account.personal_plan.PersonalPlanPresenter;
import use_case.account.personal_plan.PersonalPlanInteractor;

/** Personal-plan controller construction. */
final class PersonalPlanControllers {
    private final PersonalPlanController personalPlan;

    private PersonalPlanControllers(final PersonalPlanController personalPlan) {
        this.personalPlan = personalPlan;
    }

    static PersonalPlanControllers create(final ApplicationInfrastructure infrastructure,
                                           final FeatureModels features) {
        final GeminiPersonalPlanGenerator generator = new GeminiPersonalPlanGenerator(
            PersonalPlanControllers::personalPlanKey);
        return new PersonalPlanControllers(new PersonalPlanController(new PersonalPlanInteractor(
            infrastructure.users(), infrastructure.users(), infrastructure.washrooms(),
            new FileCalendarContentReader(), generator, new PersonalPlanPresenter(features.account()))));
    }

    private static String personalPlanKey() {
        return System.getenv(GeminiPersonalPlanGenerator.API_KEY_ENV);
    }

    PersonalPlanController personalPlan() {
        return personalPlan;
    }
}
