package use_case.account.personal_plan;

/** External planning service boundary owned by the personal-plan use case. */
public interface PersonalPlanGenerator {
    String generate(String calendarContent, int tripsPerDay) throws Exception;
}
