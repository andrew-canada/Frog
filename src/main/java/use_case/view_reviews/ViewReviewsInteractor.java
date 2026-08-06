package use_case.view_reviews;

import entity.Review;
import entity.ReviewSummary;
import entity.Washroom;
import data_access.washroom.WashroomDataAccessInterface;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;
import use_case.report_review.ReviewReportDataAccessInterface;
import use_case.vote_helpful.ReviewScorer;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

public final class ViewReviewsInteractor implements ViewReviewsInputBoundary {
    private final ReviewDataAccessInterface reviews;
    private final WashroomDataAccessInterface washrooms;
    private final HelpfulVoteDataAccessInterface votes;
    private final ReviewReportDataAccessInterface reports;
    private final ViewReviewsOutputBoundary presenter;

    public ViewReviewsInteractor(ReviewDataAccessInterface reviews, WashroomDataAccessInterface washrooms,
                                 HelpfulVoteDataAccessInterface votes, ReviewReportDataAccessInterface reports,
                                 ViewReviewsOutputBoundary presenter) {
        this.reviews = reviews;
        this.washrooms = washrooms;
        this.votes = votes;
        this.reports = reports;
        this.presenter = presenter;
    }

    @Override
    public void execute(ViewReviewsInputData input) {
        Washroom washroom = washrooms.getById(input.washroomId()).orElse(null);
        if (washroom == null) {
            presenter.presentError("Washroom not found");
            return;
        }
        ReviewSummary summary = reviews.getSummary(washroom.id());
        List<ViewReviewsOutputData.ReviewDisplay> display = reviews.getReviewsForWashroom(washroom.id()).stream()
                .sorted(Comparator.comparingDouble(ViewReviewsInteractor::score).reversed())
                .map(r -> new ViewReviewsOutputData.ReviewDisplay(r.id(), r.rating(), r.comment(), r.helpfulCount(),
                        r.createdAt(), r.authorUsername(), votes.hasVoted(r.id(), input.username()),
                        reports.hasReported(r.id(), input.username()))).toList();
        presenter.present(new ViewReviewsOutputData(washroom.id(), washroom.building().name(), displayDescription(washroom),
                summary.averageRating(), summary.averageCleanliness(), summary.reviewCount(),
                washroom.numToilets(), washroom.numSinks(), display));
    }

    private static String displayDescription(Washroom washroom) {
        String name = washroom.name();
        int separator = name.indexOf('|');
        String description = separator >= 0 ? name.substring(separator + 1) : name;
        return description.replaceAll("(?i)\\bwashrooms?\\b", "")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    /** Ranking score: helpfulness (log) + recency (exponential decay). */
    private static double score(Review review) {
        long ageInDays = ChronoUnit.DAYS.between(review.createdAt(), LocalDate.now());
        return ReviewScorer.score(review.helpfulCount(), Math.max(0, ageInDays));
    }
}
