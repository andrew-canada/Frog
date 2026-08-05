package use_case.report_review;

import java.util.List;

/** Input for reporting a review. */
public record ReportReviewInputData(String reviewId, String reporterUsername, List<String> reasons,
                                    String details) {
    public ReportReviewInputData {
        reasons = List.copyOf(reasons);
    }
}
