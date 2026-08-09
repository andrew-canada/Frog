package use_case.sort_review;

import use_case.port.ReviewRepository;
import use_case.port.CurrentUserSession;
import use_case.port.WashroomRepository;
import entity.Review;
import entity.ReviewSummary;
import entity.Washroom;
import use_case.report_review.ReviewReportDataAccessInterface;
import use_case.view_reviews.ViewReviewsOutputBoundary;
import use_case.view_reviews.ViewReviewsOutputData;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;
import use_case.vote_helpful.ReviewScorer;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class SortReviewInteractor implements SortReviewInputBoundary {
    private final ReviewRepository reviewDAO;
    private final CurrentUserSession session;
    private final WashroomRepository washroomDAO;
    private final HelpfulVoteDataAccessInterface helpfulVoteDAO;
    private final ReviewReportDataAccessInterface reportDAO;
    private final ViewReviewsOutputBoundary presenter;

    public SortReviewInteractor(ReviewRepository reviewDAO,
                                CurrentUserSession session,
                                WashroomRepository washroomDAO,
                                HelpfulVoteDataAccessInterface helpfulVoteDAO,
                                ReviewReportDataAccessInterface reportDAO,
                                ViewReviewsOutputBoundary presenter) {
        this.reviewDAO = reviewDAO;
        this.session = session;
        this.washroomDAO = washroomDAO;
        this.helpfulVoteDAO = helpfulVoteDAO;
        this.reportDAO = reportDAO;
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
    public void execute(SortReviewInputData inputData) {
        Washroom washroom = washroomDAO.getById(inputData.currentWashroom()).orElse(null);
        if (washroom == null) {
            presenter.presentError("Washroom not found");
            return;
        }

        ReviewSortOrder sortOrder = inputData.sortOrder() == null ? ReviewSortOrder.RELEVANCE : inputData.sortOrder();
        Comparator<entity.Review> comparator = switch (sortOrder) {
            case RELEVANCE -> Comparator.comparing(SortReviewInteractor::score).reversed();
            case MOST_HELPFUL -> Comparator.comparing(entity.Review::getHelpfuls).reversed();
            case HIGHEST_RATED -> Comparator.comparing(entity.Review::getStars).reversed();
            case LOWEST_RATED -> Comparator.comparing(entity.Review::getStars);
            case NEWEST -> Comparator.comparing(entity.Review::createdAt).reversed();
            case VOTED_BY_ME -> session.currentUser()
                    .<Comparator<entity.Review>>map(user -> Comparator.comparing(
                            (entity.Review review) -> review.authorUsername().equals(user.name())).reversed())
                    .orElseGet(() -> Comparator.comparing(entity.Review::getHelpfuls));
        };
        ArrayList<Review> sortedReviews = new ArrayList<>(reviewDAO.getReviewsForWashroom(washroom.id()));
        sortedReviews.sort(comparator);
        ReviewSummary summary = ReviewSummary.fromReviews(sortedReviews);
        String username = session.currentUser().map(entity.User::username).orElse("");
        Set<String> reviewIds = sortedReviews.stream().map(Review::id).collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<String> votedReviewIds = helpfulVoteDAO.votedReviewIds(reviewIds, username);
        Set<String> reportedReviewIds = reportDAO.reportedReviewIds(reviewIds, username);
        List<ViewReviewsOutputData.ReviewDisplay> display = sortedReviews.stream()
                .map(r -> new ViewReviewsOutputData.ReviewDisplay(
                        r.id(),
                        r.rating(),
                        r.comment(),
                        r.helpfulCount(),
                        r.createdAt(),
                        r.authorUsername(),
                        votedReviewIds.contains(r.id()),
                        reportedReviewIds.contains(r.id())
                )).toList();
        presenter.present(new ViewReviewsOutputData(washroom.id(), washroom.building().name(), displayDescription(washroom),
                summary.averageRating(), summary.averageCleanliness(), summary.reviewCount(),
                washroom.numToilets(), washroom.numSinks(), display));
    }
}
