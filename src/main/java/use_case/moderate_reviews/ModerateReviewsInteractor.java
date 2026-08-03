package use_case.moderate_reviews;

import entity.Report;
import entity.Review;
import use_case.moderate_reviews.ReviewAdminDataAccessInterface;
import data_access.washroom.WashroomDataAccessInterface;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The interactor for the Moderator Remove Review use case.
 * <p/>
 * Builds the reported-review queue by aggregating reports (grouped by review)
 * with the review's own data and washroom name, and lets a moderator remove a
 * review or dismiss its reports.
 */
public final class ModerateReviewsInteractor implements ModerateReviewsInputBoundary {

    private final ReportedReviewsDataAccessInterface reports;
    private final ReviewAdminDataAccessInterface reviews;
    private final WashroomDataAccessInterface washrooms;
    private final ModerateReviewsOutputBoundary presenter;

    public ModerateReviewsInteractor(ReportedReviewsDataAccessInterface reports,
                                     ReviewAdminDataAccessInterface reviews,
                                     WashroomDataAccessInterface washrooms,
                                     ModerateReviewsOutputBoundary presenter) {
        this.reports = reports;
        this.reviews = reviews;
        this.washrooms = washrooms;
        this.presenter = presenter;
    }

    @Override
    public void loadReportedReviews() {
        present(null);
    }

    @Override
    public void removeReview(ModerateReviewsInputData input) {
        reviews.deleteReview(input.reviewId());
        reports.deleteReportsForReview(input.reviewId());
        present("Review removed.");
    }

    @Override
    public void dismissReports(ModerateReviewsInputData input) {
        reports.deleteReportsForReview(input.reviewId());
        present("Reports dismissed.");
    }

    private void present(String message) {
        presenter.present(new ModerateReviewsOutputData(buildQueue(), message));
    }

    private List<ReportedReview> buildQueue() {
        // Group reports by the review they target, preserving first-seen order.
        final Map<String, List<Report>> byReview = new LinkedHashMap<>();
        for (Report report : reports.getAllReports()) {
            byReview.computeIfAbsent(report.reviewId(), key -> new ArrayList<>()).add(report);
        }

        final List<ReportedReview> queue = new ArrayList<>();
        for (Map.Entry<String, List<Report>> entry : byReview.entrySet()) {
            final Review review = reviews.getById(entry.getKey()).orElse(null);
            if (review == null) {
                continue;
            }
            queue.add(toReportedReview(review, entry.getValue()));
        }
        // Most-reported first so the worst offenders are at the top of the moderator queue.
        queue.sort(Comparator.comparingInt(ReportedReview::totalReports).reversed());
        return queue;
    }

    private ReportedReview toReportedReview(Review review, List<Report> reviewReports) {
        // Count how many reports cited each reason, preserving first-seen order.
        final Map<String, Integer> reasonTotals = new LinkedHashMap<>();
        final List<ReportedReview.AdditionalDetail> details = new ArrayList<>();
        for (Report report : reviewReports) {
            for (String reason : report.reasons()) {
                reasonTotals.merge(reason, 1, Integer::sum);
            }
            if (report.details() != null && !report.details().isBlank()) {
                final String reason = report.reasons().isEmpty() ? "Other" : report.reasons().get(0);
                details.add(new ReportedReview.AdditionalDetail(reason, report.details()));
            }
        }
        final List<ReportedReview.ReasonCount> reasonCounts = new ArrayList<>();
        reasonTotals.forEach((reason, count) ->
                reasonCounts.add(new ReportedReview.ReasonCount(reason, count)));

        final String washroomName = washrooms.getById(review.washroomId())
                .map(washroom -> washroom.name() + " — " + washroom.floor())
                .orElse(review.washroomId());

        return new ReportedReview(review.id(), washroomName, review.authorUsername(),
                review.createdAt(), review.rating(), review.comment(), reasonCounts, details);
    }
}
