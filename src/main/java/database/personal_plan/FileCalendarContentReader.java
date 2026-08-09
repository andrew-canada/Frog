package database.personal_plan;

import use_case.account.personal_plan.CalendarContentReader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Filesystem adapter for a calendar selected by the desktop UI. */
public final class FileCalendarContentReader implements CalendarContentReader {
    @Override
    public String read(String calendarPath) throws Exception {
        return Files.readString(Path.of(calendarPath));
    }
}
