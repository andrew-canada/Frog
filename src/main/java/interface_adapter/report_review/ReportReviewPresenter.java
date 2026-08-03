package interface_adapter.report_review;

import use_case.report_review.ReportReviewOutputBoundary;
import use_case.report_review.ReportReviewOutputData;

/** Presenter for the Report Review use case. */
public final class ReportReviewPresenter implements ReportReviewOutputBoundary {

    private final ReportReviewViewModel model;

    public ReportReviewPresenter(ReportReviewViewModel model) {
        this.model = model;
    }

    @Override
    public void present(ReportReviewOutputData output) {
        model.setState(new ReportReviewViewModel.State(output.message(), output.success(), true));
    }
}
