package use_case.report_review;

import entity.Report;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The interactor for the Report Review use case: validates that a reason was
 * chosen, saves the report, and returns a thank-you message.
 */
public final class ReportReviewInteractor implements ReportReviewInputBoundary {

    private static final String THANK_YOU = "Thank you for reporting, our team will take a look.";
    private static final String NO_REASON = "Please select at least one reason for reporting.";

    private final ReviewReportDataAccessInterface reports;
    private final ReportReviewOutputBoundary presenter;
    private final Clock clock;

    public ReportReviewInteractor(ReviewReportDataAccessInterface reports,
                                  ReportReviewOutputBoundary presenter) {
        this(reports, presenter, Clock.systemDefaultZone());
    }

    public ReportReviewInteractor(ReviewReportDataAccessInterface reports,
                                  ReportReviewOutputBoundary presenter, Clock clock) {
        this.reports = reports;
        this.presenter = presenter;
        this.clock = clock;
    }

    @Override
    public void report(ReportReviewInputData input) {
        if (input.reasons().isEmpty()) {
            presenter.present(new ReportReviewOutputData(false, NO_REASON));
            return;
        }
        reports.save(new Report(UUID.randomUUID().toString(), input.reviewId(),
                input.reporterUsername(), input.reasons(), input.details(),
                LocalDateTime.now(clock)));
        presenter.present(new ReportReviewOutputData(true, THANK_YOU));
    }
}
