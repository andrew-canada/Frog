package database.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import entity.MaintenanceIssue;
import entity.StatusReport;
import use_case.port.StatusReportRepository;

public final class InMemoryStatusReportDataAccessObject implements StatusReportRepository {
    private static final int MAGIC_35 = 35;
    private static final int MAGIC_4 = 4;
    private static final int MAGIC_5 = 5;
    private static final int MAGIC_3 = 3;
    private final List<StatusReport> reports = new ArrayList<>();

    public InMemoryStatusReportDataAccessObject() {
        final LocalDateTime now = LocalDateTime.now();
        reports.add(new StatusReport("bahen-2", "sheena_q", 2, MAGIC_5, MaintenanceIssue.NONE, now.minusHours(1)));
        reports.add(new StatusReport("bahen-2", null, MAGIC_4, MAGIC_4, MaintenanceIssue.NONE,
            now.minusHours(MAGIC_3)));
        reports.add(new StatusReport("robarts-4", null, MAGIC_5, MAGIC_4, MaintenanceIssue.NONE,
            now.minusMinutes(MAGIC_35)));
    }

    @Override
    public void save(final StatusReport report) {
        reports.add(report);
    }

    @Override
    public List<StatusReport> getRecentForWashroom(final String id, final LocalDateTime since) {
        return getForWashroom(id, since, LocalDateTime
            .now()
            .plusSeconds(1));
    }

    @Override
    public List<StatusReport> getForWashroom(final String id, final LocalDateTime from, final LocalDateTime to) {
        return reports
            .stream()
            .filter(reviewValue -> {
                return reviewValue
                    .washroomId()
                    .equals(id) && !reviewValue
                    .timestamp()
                    .isBefore(from) && !reviewValue
                    .timestamp()
                    .isAfter(to);
            })
            .toList();
    }
}
