package use_case.account.personal_plan;

/**
 * Reads calendar content from a UI-selected source.
 */
public interface CalendarContentReader {
    String read(String calendarPath) throws Exception;
}
