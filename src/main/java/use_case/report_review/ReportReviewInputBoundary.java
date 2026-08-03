package use_case.report_review;

/** The input boundary for the Report Review use case. */
public interface ReportReviewInputBoundary {

    /**
     * Files a report against a review.
     * @param input the review, reporter, reasons, and optional details
     */
    void report(ReportReviewInputData input);
}
