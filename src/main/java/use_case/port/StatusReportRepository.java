package use_case.port;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entity.StatusReport;

public interface StatusReportRepository {
    /**
     * Performs this operation.
     *
     * @param report parameter value.
     */
    void save(StatusReport report);

    /**
     * Performs this operation.
     *
     * @param washroomId parameter value.
     *
     * @param since parameter value.
     *
     * @return the operation result.
     */
    List<StatusReport> getRecentForWashroom(String washroomId, LocalDateTime since);

    /**
     * Performs this operation.
     *
     * @param washroomId parameter value.
     *
     * @param from parameter value.
     *
     * @param parameterValue parameter value.
     *
     * @return the operation result.
     */
    List<StatusReport> getForWashroom(String washroomId, LocalDateTime from, LocalDateTime parameterValue);

    /**
     * Returns the newest report from the current clock hour for every requested washroom.
     * @param hour parameter value.
     * @param washroomIds parameter value.
     * @return the operation result.
     */
    default Map<String, StatusReport> getCurrentHourForWashrooms(final List<String> washroomIds, final int hour) {
        final Map<String, StatusReport> result = new HashMap<>();
        final LocalDateTime since = LocalDateTime.of(2000, 1, 1, 0, 0);
        for (final String washroomId : washroomIds) {
            getRecentForWashroom(washroomId, since)
                .stream()
                .filter(report -> {
                    return report
                        .timestamp()
                        .getHour() == hour;
                })
                .max(Comparator.comparing(StatusReport::timestamp))
                .ifPresent(report -> {
                    result.put(washroomId, report);
                });
        }
        return Map.copyOf(result);
    }
}
