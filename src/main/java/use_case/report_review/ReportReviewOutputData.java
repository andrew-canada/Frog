package use_case.report_review;

/**
 * Output for the Report Review use case: whether it succeeded and a message.
 * @param message parameter value.
 * @param success parameter value.
 */
public record ReportReviewOutputData(boolean success, String message) {
}
