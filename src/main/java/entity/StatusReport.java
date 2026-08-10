package entity;

import java.time.LocalDateTime;

public record StatusReport(String washroomId, String username, int busyness, int cleanliness, MaintenanceIssue issue,
                           LocalDateTime timestamp) {
    private static final int MAX_RATING_LEVEL = 5;

    public StatusReport {
        if (washroomId == null || washroomId.isBlank()) {
            throw new IllegalArgumentException("Washroom is required");
        }
        if (busyness < 1 || busyness > MAX_RATING_LEVEL || cleanliness < 1 || cleanliness > MAX_RATING_LEVEL) {
            throw new IllegalArgumentException("Busyness and cleanliness must be from 1 to 5");
        }
        if (issue == null || timestamp == null) {
            throw new IllegalArgumentException("Issue and timestamp are required");
        }
    }
}
