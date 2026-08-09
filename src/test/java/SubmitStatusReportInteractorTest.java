import entity.MaintenanceIssue;
import entity.StatusReport;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import use_case.port.StatusReportRepository;
import use_case.status_report.SubmitStatusReportInputData;
import use_case.status_report.SubmitStatusReportInteractor;
import use_case.status_report.SubmitStatusReportOutputData;

final class SubmitStatusReportInteractorTest {
    static void run() {
        class Fake implements StatusReportRepository {
            final List<StatusReport> saved = new ArrayList<>();

            public void save(final StatusReport r) {
                saved.add(r);
            }

            public List<StatusReport> getRecentForWashroom(final String id, final LocalDateTime s) {
                return saved;
            }

            public List<StatusReport> getForWashroom(final String id, final LocalDateTime f, final LocalDateTime t) {
                return saved;
            }
        }
        final Fake fake = new Fake();
        final SubmitStatusReportOutputData[] out = new SubmitStatusReportOutputData[1];
        final Clock clock = Clock.fixed(Instant.parse("2026-07-23T12:00:00Z"), ZoneOffset.UTC);
        new SubmitStatusReportInteractor(fake, d -> out[0] = d, clock).execute(
            new SubmitStatusReportInputData("w1", 4, 5, MaintenanceIssue.NONE, null));
        TestSupport.check(out[0].success() && fake.saved.size() == 1, "valid report should save");
        TestSupport.check(out[0].currentBusyness() == 4, "updated busyness");
    }
}
