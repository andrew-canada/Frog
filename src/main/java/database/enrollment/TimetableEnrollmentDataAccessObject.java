package database.enrollment;

import java.time.DayOfWeek;
import java.util.List;

import entity.EnrollmentMeeting;
import use_case.port.EnrollmentScheduleGateway;

/**
 * Timetable API boundary with deterministic demo data; no network is required for the milestone demo.
 */
public final class TimetableEnrollmentDataAccessObject implements EnrollmentScheduleGateway {
    private static final int SMALL_CLASS_ENROLLMENT = 100;
    private static final int EVENING_END_HOUR = 18;
    private static final int AFTERNOON_END_HOUR = 16;
    private static final int SMALL_SEMINAR_ENROLLMENT = 80;
    private static final int MIDDAY_END_HOUR = 13;
    private static final int MORNING_END_HOUR = 11;
    private static final int LARGE_CLASS_ENROLLMENT = 210;
    private static final int AFTERNOON_START_HOUR = 14;
    private static final int MEDIUM_CLASS_ENROLLMENT = 120;
    private static final int MIDDAY_START_HOUR = 12;
    private static final int LATE_MORNING_START_HOUR = 10;
    private static final int LARGE_LECTURE_ENROLLMENT = 240;
    private static final int LATE_AFTERNOON_END_HOUR = 17;
    private static final int LATE_AFTERNOON_START_HOUR = 15;
    private static final int VERY_LARGE_LECTURE_ENROLLMENT = 360;
    private static final int LARGE_CLASSROOM_ENROLLMENT = 180;
    private static final int MORNING_START_HOUR = 9;

    @Override
    public List<EnrollmentMeeting> getBuildingSchedule(final String code, final DayOfWeek day) {
        return switch (code) {
            case "BA" -> List.of(
                new EnrollmentMeeting(MORNING_START_HOUR, MORNING_END_HOUR, LARGE_CLASSROOM_ENROLLMENT),
                new EnrollmentMeeting(MIDDAY_START_HOUR, AFTERNOON_START_HOUR, VERY_LARGE_LECTURE_ENROLLMENT),
                new EnrollmentMeeting(LATE_AFTERNOON_START_HOUR, LATE_AFTERNOON_END_HOUR,
                    LARGE_LECTURE_ENROLLMENT));
            case "RB" -> List.of(
                new EnrollmentMeeting(LATE_MORNING_START_HOUR, MIDDAY_START_HOUR, MEDIUM_CLASS_ENROLLMENT),
                new EnrollmentMeeting(AFTERNOON_START_HOUR, AFTERNOON_END_HOUR, LARGE_CLASS_ENROLLMENT));
            default -> List.of(
                new EnrollmentMeeting(MORNING_END_HOUR, MIDDAY_END_HOUR, SMALL_SEMINAR_ENROLLMENT),
                new EnrollmentMeeting(AFTERNOON_END_HOUR, EVENING_END_HOUR, SMALL_CLASS_ENROLLMENT));
        };
    }
}
