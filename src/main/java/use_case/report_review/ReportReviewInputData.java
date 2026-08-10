package use_case.report_review;

import java.util.List;

/**
 * Input for reporting a review.
 * @param details parameter value.
 * @param reasons parameter value.
 * @param reporterUsername parameter value.
 * @param reviewId parameter value.
 */
public record ReportReviewInputData(String reviewId, String reporterUsername, List<String> reasons, String details) {
    public ReportReviewInputData {
        reasons = List.copyOf(reasons);
    }
}
