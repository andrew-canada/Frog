package interface_adapter.report_review;

import interface_adapter.common.ViewModel;

/** ViewModel for the Report Review dialog. */
public final class ReportReviewViewModel extends ViewModel<ReportReviewViewModel.State> {

    public ReportReviewViewModel() {
        super(new State("", false, false));
    }

    /**
     * @param message a status/confirmation message
     * @param success whether the report was accepted
     * @param submitted whether a submission has happened (to trigger the view)
     */
    public record State(String message, boolean success, boolean submitted) { }
}
