package use_case.view_reviews;

import use_case.port.ReviewRepository;
import use_case.port.WashroomRepository;
import entity.Review;
import entity.ReviewSummary;
import entity.Washroom;
import use_case.report_review.ReviewReportDataAccessInterface;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;
import use_case.vote_helpful.ReviewScorer;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class ViewReviewsInteractor implements ViewReviewsInputBoundary {
    private final ReviewRepository reviews;
    private final WashroomRepository washrooms;
    private final HelpfulVoteDataAccessInterface votes;
    private final ReviewReportDataAccessInterface reports;
    private final ViewReviewsOutputBoundary presenter;

    public ViewReviewsInteractor(ReviewRepository reviews, WashroomRepository washrooms,
                                 HelpfulVoteDataAccessInterface votes, ReviewReportDataAccessInterface reports,
                                 ViewReviewsOutputBoundary presenter) {
        this.reviews = reviews;
        this.washrooms = washrooms;
        this.votes = votes;
        this.reports = reports;
        this.presenter = presenter;
    }

    private static String displayDescription(Washroom washroom) {
        String name = washroom.name();
        int separator = name.indexOf('|');
        String description = separator >= 0 ? name.substring(separator + 1) : name;
        return description.replaceAll("(?i)\\bwashrooms?\\b", "")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    /**
     * Ranking score: helpfulness (log) + recency (exponential decay).
     */
    private static double score(Review review) {
        long ageInDays = ChronoUnit.DAYS.between(review.createdAt(), LocalDate.now());
        return ReviewScorer.score(review.helpfulCount(), Math.max(0, ageInDays));
    }

    @Override
    public void execute(ViewReviewsInputData input) {
        Washroom washroom = washrooms.getById(input.washroomId()).orElse(null);
        if (washroom == null) {
            presenter.presentError("Washroom not found");
            return;
        }
        List<Review> washroomReviews = reviews.getReviewsForWashroom(washroom.id());
        ReviewSummary summary = ReviewSummary.fromReviews(washroomReviews);
        Set<String> reviewIds = washroomReviews.stream().map(Review::id).collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<String> votedReviewIds = votes.votedReviewIds(reviewIds, input.username());
        Set<String> reportedReviewIds = reports.reportedReviewIds(reviewIds, input.username());
        List<ViewReviewsOutputData.ReviewDisplay> display = washroomReviews.stream()
                .sorted(Comparator.comparingDouble(ViewReviewsInteractor::score).reversed())
                .map(r -> new ViewReviewsOutputData.ReviewDisplay(r.id(), r.rating(), r.comment(), r.helpfulCount(),
                        r.createdAt(), r.authorUsername(), votedReviewIds.contains(r.id()),
                        reportedReviewIds.contains(r.id()))).toList();
        presenter.present(new ViewReviewsOutputData(washroom.id(), washroom.building().name(), displayDescription(washroom),
                summary.averageRating(), summary.averageCleanliness(), summary.reviewCount(),
                washroom.numToilets(), washroom.numSinks(), display));
    }
}
