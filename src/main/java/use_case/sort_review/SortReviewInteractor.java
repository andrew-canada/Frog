package use_case.sort_review;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import entity.Review;
import entity.ReviewSummary;
import entity.Washroom;
import use_case.port.CurrentUserSession;
import use_case.port.ReviewRepository;
import use_case.port.WashroomRepository;
import use_case.report_review.ReviewReportDataAccessInterface;
import use_case.view_reviews.ViewReviewsOutputBoundary;
import use_case.view_reviews.ViewReviewsOutputData;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;
import use_case.vote_helpful.ReviewScorer;

public class SortReviewInteractor implements SortReviewInputBoundary {
    private final ReviewRepository reviewDAO;
    private final CurrentUserSession session;
    private final WashroomRepository washroomDAO;
    private final HelpfulVoteDataAccessInterface helpfulVoteDAO;
    private final ReviewReportDataAccessInterface reportDAO;
    private final ViewReviewsOutputBoundary presenter;

    public SortReviewInteractor(final ReviewRepository reviewDAO, final CurrentUserSession session,
                                final WashroomRepository washroomDAO,
                                final HelpfulVoteDataAccessInterface helpfulVoteDAO,
                                final ReviewReportDataAccessInterface reportDAO,
                                final ViewReviewsOutputBoundary presenter) {
        this.reviewDAO = reviewDAO;
        this.session = session;
        this.washroomDAO = washroomDAO;
        this.helpfulVoteDAO = helpfulVoteDAO;
        this.reportDAO = reportDAO;
        this.presenter = presenter;

    }

    private static String displayDescription(final Washroom washroom) {
        final String name = washroom.name();
        final int separator = name.indexOf('|');
        final String description;
        if (separator >= 0) {
            description = name.substring(separator + 1);
        }
        else {
            description = name;
        }
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
    public void execute(final SortReviewInputData inputData) {
        final Washroom washroom = washroomDAO
            .getById(inputData.currentWashroom())
            .orElse(null);
        if (washroom == null) {
            presenter.presentError("Washroom not found");
            return;
        }

        final ReviewSortOrder sortOrder;
        if (inputData.sortOrder() == null) {
            sortOrder = ReviewSortOrder.RELEVANCE;
        }
        else {
            sortOrder = inputData.sortOrder();
        }
        final Comparator<entity.Review> comparator = switch (sortOrder) {
            case RELEVANCE -> Comparator
                .comparing(SortReviewInteractor::score)
                .reversed();
            case MOST_HELPFUL -> Comparator
                .comparing(entity.Review::getHelpfuls)
                .reversed();
            case HIGHEST_RATED -> Comparator
                .comparing(entity.Review::getStars)
                .reversed();
            case LOWEST_RATED -> Comparator.comparing(entity.Review::getStars);
            case NEWEST -> Comparator
                .comparing(entity.Review::createdAt)
                .reversed();
            case VOTED_BY_ME -> session
                .currentUser()
                .map(user -> {
                    return Comparator
                        .comparing((Review review) -> {
                            return review
                                .authorUsername()
                                .equals(user.name());
                        })
                        .reversed();
                })
                .orElseGet(() -> {
                    return Comparator.comparing(Review::getHelpfuls);
                });
        };
        final ArrayList<Review> sortedReviews = new ArrayList<>(reviewDAO.getReviewsForWashroom(washroom.id()));
        sortedReviews.sort(comparator);
        final ReviewSummary summary = ReviewSummary.fromReviews(sortedReviews);
        final String username = session
            .currentUser()
            .map(entity.User::username)
            .orElse("");
        final Set<String> reviewIds = sortedReviews
            .stream()
            .map(Review::id)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
        final Set<String> votedReviewIds = helpfulVoteDAO.votedReviewIds(reviewIds, username);
        final Set<String> reportedReviewIds = reportDAO.reportedReviewIds(reviewIds, username);
        final List<ViewReviewsOutputData.ReviewDisplay> display = sortedReviews
            .stream()
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
