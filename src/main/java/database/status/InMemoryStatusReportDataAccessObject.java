package database.status;

import entity.MaintenanceIssue;
import entity.StatusReport;
import use_case.port.StatusReportRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class InMemoryStatusReportDataAccessObject implements StatusReportRepository {
    private final List<StatusReport> reports = new ArrayList<>();

    public InMemoryStatusReportDataAccessObject() {
        LocalDateTime now = LocalDateTime.now();
        reports.add(new StatusReport("bahen-2", "sheena_q", 2, 5, MaintenanceIssue.NONE, now.minusHours(1)));
        reports.add(new StatusReport("bahen-2", null, 4, 4, MaintenanceIssue.NONE, now.minusHours(3)));
        reports.add(new StatusReport("robarts-4", null, 5, 4, MaintenanceIssue.NONE, now.minusMinutes(35)));
    }

    @Override
    public void save(StatusReport report) {
        reports.add(report);
    }

    @Override
    public List<StatusReport> getRecentForWashroom(String id, LocalDateTime since) {
        return getForWashroom(id, since, LocalDateTime.now().plusSeconds(1));
    }

    @Override
    public List<StatusReport> getForWashroom(String id, LocalDateTime from, LocalDateTime to) {
        return reports.stream().filter(r -> r.washroomId().equals(id)
                && !r.timestamp().isBefore(from) && !r.timestamp().isAfter(to)).toList();
    }
}
