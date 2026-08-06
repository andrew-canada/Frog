package use_case.gateway;

import entity.EnrollmentMeeting;

import java.time.DayOfWeek;
import java.util.List;

public interface EnrollmentDataAccessInterface {
    List<EnrollmentMeeting> getBuildingSchedule(String buildingCode, DayOfWeek dayOfWeek);
}
