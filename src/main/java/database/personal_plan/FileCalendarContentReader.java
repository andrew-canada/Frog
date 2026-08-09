package database.personal_plan;

import java.nio.file.Files;
import java.nio.file.Path;

import use_case.account.personal_plan.CalendarContentReader;

/**
 * Filesystem adapter for a calendar selected by the desktop UI.
 */
public final class FileCalendarContentReader implements CalendarContentReader {
    @Override
    public String read(final String calendarPath) throws Exception {
        return Files.readString(Path.of(calendarPath));
    }
}
