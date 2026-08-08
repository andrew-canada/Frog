package use_case.report_review;

/**
 * Output for the Report Review use case: whether it succeeded and a message.
 */
public record ReportReviewOutputData(boolean success, String message) {
}
