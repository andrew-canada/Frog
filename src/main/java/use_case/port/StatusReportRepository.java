package use_case.port;

import entity.StatusReport;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface StatusReportRepository {
    void save(StatusReport report);

    List<StatusReport> getRecentForWashroom(String washroomId, LocalDateTime since);

    List<StatusReport> getForWashroom(String washroomId, LocalDateTime from, LocalDateTime to);

    /**
     * Returns the newest report from the current clock hour for every requested washroom.
     */
    default Map<String, StatusReport> getCurrentHourForWashrooms(final List<String> washroomIds, final int hour) {
        final Map<String, StatusReport> result = new HashMap<>();
        final LocalDateTime since = LocalDateTime.of(2000, 1, 1, 0, 0);
        for (final String washroomId : washroomIds) {
            getRecentForWashroom(washroomId, since)
                .stream()
                .filter(report -> report
                    .timestamp()
                    .getHour() == hour)
                .max(Comparator.comparing(StatusReport::timestamp))
                .ifPresent(report -> result.put(washroomId, report));
        }
        return Map.copyOf(result);
    }
}
