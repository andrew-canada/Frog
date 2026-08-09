package entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A report filed against a review by a user, citing one or more reasons and an
 * optional written explanation.
 */
public record Report(String id, String reviewId, String reporterUsername, List<String> reasons, String details,
                     LocalDateTime createdAt) {
    public Report {
        reasons = List.copyOf(reasons);
    }
}
