package use_case.port;

import java.time.DayOfWeek;
import java.util.List;

import entity.EnrollmentMeeting;

public interface EnrollmentScheduleGateway {
    List<EnrollmentMeeting> getBuildingSchedule(String buildingCode, DayOfWeek dayOfWeek);
}
