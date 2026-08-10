package interface_adapter.report_review;

import java.util.List;

import use_case.report_review.ReportReviewInputBoundary;
import use_case.report_review.ReportReviewInputData;

/**
 * Controller for the Report Review use case.
 */
public final class ReportReviewController {

    private final ReportReviewInputBoundary interactor;

    public ReportReviewController(final ReportReviewInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Performs this operation.
     *
     * @param details parameter value.
     *
     * @param reasons parameter value.
     *
     * @param reporterUsername parameter value.
     *
     * @param reviewId parameter value.
     */
    public void report(final String reviewId, final String reporterUsername, final List<String> reasons,
                       final String details) {
        interactor.report(new ReportReviewInputData(reviewId, reporterUsername, reasons, details));
    }
}
