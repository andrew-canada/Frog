import entity.*;
import use_case.busyness.*;
import use_case.gateway.*;

import java.time.*;
import java.util.*;

final class BusynessStatsInteractorTest {
    static void run() {
        StatusReportDataAccessInterface reports = new StatusReportDataAccessInterface() {
            public void save(StatusReport r) {
            }

            public List<StatusReport> getRecentForWashroom(String id, LocalDateTime s) {
                return List.of();
            }

            public List<StatusReport> getForWashroom(String id, LocalDateTime f, LocalDateTime t) {
                return List.of();
            }
        };
        EnrollmentDataAccessInterface enrollment = (code, day) -> List.of(new EnrollmentMeeting(9, 11, 300));
        final BusynessStatsOutputData[] out = new BusynessStatsOutputData[1];
        new BusynessStatsInteractor(reports, enrollment, d -> out[0] = d).execute(new BusynessStatsInputData("w1", "BA", DayOfWeek.THURSDAY));
        TestSupport.check(out[0].buckets().size() == 13, "hourly buckets");
        TestSupport.check(out[0].buckets().get(1).busynessLevel() > out[0].buckets().get(0).busynessLevel(), "enrollment raises predicted traffic");
    }
}
