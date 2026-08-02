import entity.StatusReport;
import use_case.gateway.StatusReportDataAccessInterface;
import use_case.status_report.*;
import entity.MaintenanceIssue;

import java.time.*;
import java.util.*;

final class SubmitStatusReportInteractorTest {
    static void run() {
        class Fake implements StatusReportDataAccessInterface {
            final List<StatusReport> saved = new ArrayList<>();

            public void save(StatusReport r) {
                saved.add(r);
            }

            public List<StatusReport> getRecentForWashroom(String id, LocalDateTime s) {
                return saved;
            }

            public List<StatusReport> getForWashroom(String id, LocalDateTime f, LocalDateTime t) {
                return saved;
            }
        }
        Fake fake = new Fake();
        final SubmitStatusReportOutputData[] out = new SubmitStatusReportOutputData[1];
        Clock clock = Clock.fixed(Instant.parse("2026-07-23T12:00:00Z"), ZoneOffset.UTC);
        new SubmitStatusReportInteractor(fake, d -> out[0] = d, clock).execute(new SubmitStatusReportInputData("w1", 4, 5, MaintenanceIssue.NONE, null));
        TestSupport.check(out[0].success() && fake.saved.size() == 1, "valid report should save");
        TestSupport.check(out[0].currentBusyness() == 4, "updated busyness");
    }
}
