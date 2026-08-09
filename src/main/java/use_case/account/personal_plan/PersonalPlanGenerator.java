package use_case.account.personal_plan;

import entity.Washroom;

import java.util.List;

/** External planning service boundary owned by the personal-plan use case. */
public interface PersonalPlanGenerator {
    String generate(String calendarContent, int tripsPerDay, String semester,
                    List<Washroom> availableWashrooms) throws Exception;
}
