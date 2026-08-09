package use_case.moderate_reviews;

import entity.Report;
import entity.Review;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import use_case.port.WashroomRepository;

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
    private final WashroomRepository washrooms;
    private final ModeratorDataAccessInterface moderators;
    private final ModerateReviewsOutputBoundary presenter;

    public ModerateReviewsInteractor(final ReportedReviewsDataAccessInterface reports,
                                     final ReviewAdminDataAccessInterface reviews,
                                     final WashroomRepository washrooms,
                                     final ModeratorDataAccessInterface moderators,
                                     final ModerateReviewsOutputBoundary presenter) {
        this.reports = reports;
        this.reviews = reviews;
        this.washrooms = washrooms;
        this.moderators = moderators;
        this.presenter = presenter;
    }

    @Override
    public void loadReportedReviews() {
        present(null);
    }

    @Override
    public void removeReview(final ModerateReviewsInputData input) {
        if (denyUnlessModerator(input)) {
            return;
        }
        reviews.deleteReview(input.reviewId());
        reports.deleteReportsForReview(input.reviewId());
        present("Review removed.");
    }

    @Override
    public void dismissReports(final ModerateReviewsInputData input) {
        if (denyUnlessModerator(input)) {
            return;
        }
        reports.deleteReportsForReview(input.reviewId());
        present("Reports dismissed.");
    }

    /**
     * Authorization guard for moderator actions. When the acting user is not a
     * moderator, presents an empty queue with a "Not authorized." message (so no
     * reported reviews are exposed) and reports that the action was blocked.
     *
     * @return true if the action must be aborted
     */
    private boolean denyUnlessModerator(final ModerateReviewsInputData input) {
        if (moderators.isModerator(input.moderatorUsername())) {
            return false;
        }
        presenter.present(new ModerateReviewsOutputData(java.util.List.of(), "Not authorized."));
        return true;
    }

    private void present(final String message) {
        presenter.present(new ModerateReviewsOutputData(buildQueue(), message));
    }

    private List<ReportedReview> buildQueue() {
        // Group reports by the review they target, preserving first-seen order.
        final Map<String, List<Report>> byReview = new LinkedHashMap<>();
        for (final Report report : reports.getAllReports()) {
            byReview
                .computeIfAbsent(report.reviewId(), key -> new ArrayList<>())
                .add(report);
        }

        final List<ReportedReview> queue = new ArrayList<>();
        for (final Map.Entry<String, List<Report>> entry : byReview.entrySet()) {
            final Review review = reviews
                .getById(entry.getKey())
                .orElse(null);
            if (review == null) {
                continue;
            }
            queue.add(toReportedReview(review, entry.getValue()));
        }
        // Most-reported first so the worst offenders are at the top of the moderator queue.
        queue.sort(Comparator
            .comparingInt(ReportedReview::totalReports)
            .reversed());
        return queue;
    }

    private ReportedReview toReportedReview(final Review review, final List<Report> reviewReports) {
        // Count how many reports cited each reason, preserving first-seen order.
        final Map<String, Integer> reasonTotals = new LinkedHashMap<>();
        final List<ReportedReview.AdditionalDetail> details = new ArrayList<>();
        for (final Report report : reviewReports) {
            for (final String reason : report.reasons()) {
                reasonTotals.merge(reason, 1, Integer::sum);
            }
            if (report.details() != null && !report
                .details()
                .isBlank()) {
                final String reason = report
                    .reasons()
                    .isEmpty() ? "Other" : report
                                           .reasons()
                                           .get(0);
                details.add(new ReportedReview.AdditionalDetail(reason, report.details()));
            }
        }
        final List<ReportedReview.ReasonCount> reasonCounts = new ArrayList<>();
        reasonTotals.forEach((reason, count) ->
            reasonCounts.add(new ReportedReview.ReasonCount(reason, count)));

        final String washroomName = washrooms
            .getById(review.washroomId())
            .map(washroom -> washroom.name() + " — " + washroom.floor())
            .orElse(review.washroomId());

        return new ReportedReview(review.id(), washroomName, review.authorUsername(),
            review.createdAt(), review.rating(), review.comment(), reasonCounts, details);
    }
}
