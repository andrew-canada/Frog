package use_case.view_reviews;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import entity.Review;
import entity.ReviewSummary;
import entity.Washroom;
import use_case.port.ReviewRepository;
import use_case.port.WashroomRepository;
import use_case.report_review.ReviewReportDataAccessInterface;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;
import use_case.vote_helpful.ReviewScorer;

public final class ViewReviewsInteractor implements ViewReviewsInputBoundary {
    private final ReviewRepository reviews;
    private final WashroomRepository washrooms;
    private final HelpfulVoteDataAccessInterface votes;
    private final ReviewReportDataAccessInterface reports;
    private final ViewReviewsOutputBoundary presenter;

    public ViewReviewsInteractor(final ReviewRepository reviews, final WashroomRepository washrooms,
                                 final HelpfulVoteDataAccessInterface votes,
                                 final ReviewReportDataAccessInterface reports,
                                 final ViewReviewsOutputBoundary presenter) {
        this.reviews = reviews;
        this.washrooms = washrooms;
        this.votes = votes;
        this.reports = reports;
        this.presenter = presenter;
    }

    private static String displayDescription(final Washroom washroom) {
        final String name = washroom.name();
        final int separator = name.indexOf('|');
        final String description = separator >= 0 ? name.substring(separator + 1) : name;
        return description
            .replaceAll("(?i)\\bwashrooms?\\b", "")
            .replaceAll("\\s{2,}", " ")
            .trim();
    }

    /**
     * Ranking score: helpfulness (log) + recency (exponential decay).
     */
    private static double score(final Review review) {
        final long ageInDays = ChronoUnit.DAYS.between(review.createdAt(), LocalDate.now());
        return ReviewScorer.score(review.helpfulCount(), Math.max(0, ageInDays));
    }

    @Override
    public void execute(final ViewReviewsInputData input) {
        final Washroom washroom = washrooms
            .getById(input.washroomId())
            .orElse(null);
        if (washroom == null) {
            presenter.presentError("Washroom not found");
            return;
        }
        final List<Review> washroomReviews = reviews.getReviewsForWashroom(washroom.id());
        final ReviewSummary summary = ReviewSummary.fromReviews(washroomReviews);
        final Set<String> reviewIds = washroomReviews
            .stream()
            .map(Review::id)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
        final Set<String> votedReviewIds = votes.votedReviewIds(reviewIds, input.username());
        final Set<String> reportedReviewIds = reports.reportedReviewIds(reviewIds, input.username());
        final List<ViewReviewsOutputData.ReviewDisplay> display = washroomReviews
            .stream()
            .sorted(Comparator
                .comparingDouble(ViewReviewsInteractor::score)
                .reversed())
            .map(r -> {
                return new ViewReviewsOutputData.ReviewDisplay(r.id(), r.rating(), r.comment(), r.helpfulCount(), r.createdAt(), r.authorUsername(), votedReviewIds.contains(r.id()),
                    reportedReviewIds.contains(r.id()));
            })
            .toList();
        presenter.present(new ViewReviewsOutputData(washroom.id(), washroom
            .building()
            .name(), displayDescription(washroom), summary.averageRating(), summary.averageCleanliness(),
            summary.reviewCount(), washroom.numToilets(), washroom.numSinks(), display));
    }
}
