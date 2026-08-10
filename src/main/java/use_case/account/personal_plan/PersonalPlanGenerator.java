package use_case.account.personal_plan;

import java.util.List;

import entity.Washroom;

/**
 * External planning service boundary owned by the personal-plan use case.
 */
public interface PersonalPlanGenerator {
    /**
     * Performs this operation.
     *
     * @param availableWashrooms parameter value.
     *
     * @param calendarContent parameter value.
     *
     * @param semester parameter value.
     *
     * @param tripsPerDay parameter value.
     *
     * @return the operation result.
     *
     */
    String generate(String calendarContent, int tripsPerDay, String semester, List<Washroom> availableWashrooms);
}
