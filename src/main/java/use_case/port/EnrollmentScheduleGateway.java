package use_case.port;

import java.time.DayOfWeek;
import java.util.List;

import entity.EnrollmentMeeting;

public interface EnrollmentScheduleGateway {
    /**
     * Performs this operation.
     *
     * @param buildingCode parameter value.
     *
     * @param dayOfWeek parameter value.
     *
     * @return the operation result.
     */
    List<EnrollmentMeeting> getBuildingSchedule(String buildingCode, DayOfWeek dayOfWeek);
}
