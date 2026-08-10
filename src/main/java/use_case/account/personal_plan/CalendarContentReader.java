package use_case.account.personal_plan;

/**
 * Reads calendar content from a UI-selected source.
 */
public interface CalendarContentReader {
    /**
     * Performs this operation.
     *
     * @param calendarPath parameter value.
     *
     * @return the operation result.
     *
     * @throws java.io.IOException if the operation fails.
     */
    String read(String calendarPath) throws java.io.IOException;
}
