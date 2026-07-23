package use_case.gateway;

import entity.StatusReport;
import java.time.LocalDateTime;
import java.util.List;

public interface StatusReportDataAccessInterface {
    void save(StatusReport report);
    List<StatusReport> getRecentForWashroom(String washroomId, LocalDateTime since);
    List<StatusReport> getForWashroom(String washroomId, LocalDateTime from, LocalDateTime to);
}
