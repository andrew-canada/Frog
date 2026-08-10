package use_case.moderate_reviews;

import java.util.List;

/**
 * Output: the current reported-review queue plus an optional status message.
 * @param message parameter value.
 * @param reportedReviews parameter value.
 */
public record ModerateReviewsOutputData(List<ReportedReview> reportedReviews, String message) {
    public ModerateReviewsOutputData {
        reportedReviews = List.copyOf(reportedReviews);
    }
}
