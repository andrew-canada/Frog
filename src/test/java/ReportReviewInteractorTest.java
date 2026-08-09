import entity.Report;
import use_case.report_review.*;

import java.util.ArrayList;
import java.util.List;

final class ReportReviewInteractorTest {

    static void run() {
        reportValidatesAndSaves();
    }

    private static void reportValidatesAndSaves() {
        final List<Report> saved = new ArrayList<>();
        ReviewReportDataAccessInterface store = new ReviewReportDataAccessInterface() {
            public void save(Report report) {
                saved.add(report);
            }

            public boolean hasReported(String reviewId, String user) {
                return saved.stream().anyMatch(r -> r.reviewId().equals(reviewId) && r.reporterUsername().equals(user));
            }
        };
        final ReportReviewOutputData[] out = new ReportReviewOutputData[1];
        ReportReviewInputBoundary reporter = new ReportReviewInteractor(store, d -> out[0] = d);

        reporter.report(new ReportReviewInputData("r1", "user2", List.of(), ""));
        TestSupport.check(!out[0].success(), "report with no reason rejected");
        TestSupport.check(saved.isEmpty(), "nothing saved when the report is rejected");

        reporter.report(new ReportReviewInputData("r1", "user2", List.of("Spam", "Other"), "wrong washroom"));
        TestSupport.check(out[0].success(), "report accepted once a reason is chosen");
        TestSupport.check(saved.size() == 1, "report saved");
        TestSupport.check(store.hasReported("r1", "user2"), "hasReported true for the reporter");
        TestSupport.check(!store.hasReported("r1", "user3"), "hasReported false for a different user");
        TestSupport.check(!store.hasReported("r2", "user2"), "hasReported false for an unreported review");
    }
}
