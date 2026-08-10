package database.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import entity.MaintenanceIssue;
import entity.StatusReport;
import use_case.port.StatusReportRepository;

public final class InMemoryStatusReportDataAccessObject implements StatusReportRepository {
    private static final int RECENT_REPORT_AGE_MINUTES = 35;
    private static final int TYPICAL_STATUS_LEVEL = 4;
    private static final int MAX_STATUS_LEVEL = 5;
    private static final int OLDER_REPORT_AGE_HOURS = 3;
    private final List<StatusReport> reports = new ArrayList<>();

    public InMemoryStatusReportDataAccessObject() {
        final LocalDateTime now = LocalDateTime.now();
        reports.add(new StatusReport("bahen-2", "sheena_q", 2, MAX_STATUS_LEVEL, MaintenanceIssue.NONE,
            now.minusHours(1)));
        reports.add(new StatusReport("bahen-2", null, TYPICAL_STATUS_LEVEL, TYPICAL_STATUS_LEVEL, MaintenanceIssue.NONE,
            now.minusHours(OLDER_REPORT_AGE_HOURS)));
        reports.add(new StatusReport("robarts-4", null, MAX_STATUS_LEVEL, TYPICAL_STATUS_LEVEL, MaintenanceIssue.NONE,
            now.minusMinutes(RECENT_REPORT_AGE_MINUTES)));
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
