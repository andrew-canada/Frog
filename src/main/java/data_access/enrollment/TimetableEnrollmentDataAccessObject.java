package data_access.enrollment;

import entity.EnrollmentMeeting;
import use_case.gateway.EnrollmentDataAccessInterface;
import java.time.DayOfWeek;
import java.util.List;

/** Timetable API boundary with deterministic demo data; no network is required for the milestone demo. */
public final class TimetableEnrollmentDataAccessObject implements EnrollmentDataAccessInterface {
    @Override public List<EnrollmentMeeting> getBuildingSchedule(String code, DayOfWeek day) {
        return switch (code) {
            case "BA" -> List.of(new EnrollmentMeeting(9, 11, 180), new EnrollmentMeeting(12, 14, 360),
                    new EnrollmentMeeting(15, 17, 240));
            case "RB" -> List.of(new EnrollmentMeeting(10, 12, 120), new EnrollmentMeeting(14, 16, 210));
            default -> List.of(new EnrollmentMeeting(11, 13, 80), new EnrollmentMeeting(16, 18, 100));
        };
    }
}
