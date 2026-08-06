package interface_adapter.report_review;

import use_case.report_review.ReportReviewInputBoundary;
import use_case.report_review.ReportReviewInputData;

import java.util.List;

/**
 * Controller for the Report Review use case.
 */
public final class ReportReviewController {

    private final ReportReviewInputBoundary interactor;

    public ReportReviewController(ReportReviewInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void report(String reviewId, String reporterUsername, List<String> reasons, String details) {
        interactor.report(new ReportReviewInputData(reviewId, reporterUsername, reasons, details));
    }
}
