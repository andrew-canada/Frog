import data_access.enrollment.EnrollmentDataAccessInterface;
import data_access.status.StatusReportDataAccessInterface;
import entity.EnrollmentMeeting;
import entity.MaintenanceIssue;
import entity.StatusReport;
import use_case.busyness.BusynessStatsInputData;
import use_case.busyness.BusynessStatsInteractor;
import use_case.busyness.BusynessStatsOutputData;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

final class BusynessStatsInteractorTest {
    static void run() {
        StatusReportDataAccessInterface reports = new StatusReportDataAccessInterface() {
            public void save(StatusReport r) {
            }

            public List<StatusReport> getRecentForWashroom(String id, LocalDateTime s) {
                return List.of();
            }

            public List<StatusReport> getForWashroom(String id, LocalDateTime f, LocalDateTime t) {
                return List.of(
                        new StatusReport(id, "older", 1, 5, MaintenanceIssue.NONE, LocalDateTime.of(2026, 8, 4, 9, 0)),
                        new StatusReport(id, "newer", 5, 2, MaintenanceIssue.NONE, LocalDateTime.of(2026, 8, 5, 9, 0)));
            }
        };
        EnrollmentDataAccessInterface enrollment = (code, day) -> List.of(new EnrollmentMeeting(9, 11, 300));
        final BusynessStatsOutputData[] out = new BusynessStatsOutputData[1];
        new BusynessStatsInteractor(reports, enrollment, d -> out[0] = d).execute(new BusynessStatsInputData("w1", "BA", DayOfWeek.THURSDAY));
        TestSupport.check(out[0].buckets().size() == 24, "hourly buckets");
        TestSupport.check(out[0].buckets().get(9).busynessLevel() == 5, "latest report sets busyness");
        TestSupport.check(out[0].buckets().get(9).cleanlinessLevel() == 2, "latest report sets cleanliness");
    }
}
