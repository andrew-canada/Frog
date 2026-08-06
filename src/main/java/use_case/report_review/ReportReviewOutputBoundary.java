package use_case.report_review;

/** The output boundary for the Report Review use case. */
public interface ReportReviewOutputBoundary {

    /**
     * Presents the outcome of a report submission.
     * @param output success flag and message
     */
    void present(ReportReviewOutputData output);
}
