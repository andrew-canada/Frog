package use_case.port;

import entity.EnrollmentMeeting;

import java.time.DayOfWeek;
import java.util.List;

public interface EnrollmentScheduleGateway {
    List<EnrollmentMeeting> getBuildingSchedule(String buildingCode, DayOfWeek dayOfWeek);
}
