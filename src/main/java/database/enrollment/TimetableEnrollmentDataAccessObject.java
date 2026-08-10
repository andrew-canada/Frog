package database.enrollment;

import java.time.DayOfWeek;
import java.util.List;

import entity.EnrollmentMeeting;
import use_case.port.EnrollmentScheduleGateway;

/**
 * Timetable API boundary with deterministic demo data; no network is required for the milestone demo.
 */
public final class TimetableEnrollmentDataAccessObject implements EnrollmentScheduleGateway {
    private static final int MAGIC_100 = 100;
    private static final int MAGIC_18 = 18;
    private static final int MAGIC_16 = 16;
    private static final int MAGIC_80 = 80;
    private static final int MAGIC_13 = 13;
    private static final int MAGIC_11 = 11;
    private static final int MAGIC_210 = 210;
    private static final int MAGIC_14 = 14;
    private static final int MAGIC_120 = 120;
    private static final int MAGIC_12 = 12;
    private static final int MAGIC_10 = 10;
    private static final int MAGIC_240 = 240;
    private static final int MAGIC_17 = 17;
    private static final int MAGIC_15 = 15;
    private static final int MAGIC_360 = 360;
    private static final int MAGIC_180 = 180;
    private static final int MAGIC_9 = 9;

    @Override
    public List<EnrollmentMeeting> getBuildingSchedule(final String code, final DayOfWeek day) {
        return switch (code) {
            case "BA" -> List.of(new EnrollmentMeeting(MAGIC_9, MAGIC_11, MAGIC_180), new EnrollmentMeeting(MAGIC_12,
                MAGIC_14, MAGIC_360),
                new EnrollmentMeeting(MAGIC_15, MAGIC_17, MAGIC_240));
            case "RB" -> List.of(new EnrollmentMeeting(MAGIC_10, MAGIC_12, MAGIC_120), new EnrollmentMeeting(MAGIC_14,
                MAGIC_16, MAGIC_210));
            default -> List.of(new EnrollmentMeeting(MAGIC_11, MAGIC_13, MAGIC_80), new EnrollmentMeeting(MAGIC_16,
                MAGIC_18, MAGIC_100));
        };
    }
}
