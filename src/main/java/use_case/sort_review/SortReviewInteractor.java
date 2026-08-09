package use_case.sort_review;

import data_access.user.UserDataAccessInterface;
import data_access.washroom.WashroomDataAccessInterface;
import entity.Review;
import entity.ReviewSummary;
import entity.Washroom;
import use_case.report_review.ReviewReportDataAccessInterface;
import use_case.sort_washrooms.SortWashroomsOutputBoundary;
import use_case.sort_washrooms.SortWashroomsOutputData;
import use_case.view_reviews.ReviewDataAccessInterface;
import use_case.view_reviews.ViewReviewsInteractor;
import use_case.view_reviews.ViewReviewsOutputBoundary;
import use_case.view_reviews.ViewReviewsOutputData;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;
import use_case.vote_helpful.ReviewScorer;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortReviewInteractor implements SortReviewInputBoundary{
    private final ReviewDataAccessInterface reviewDAO;
    private final UserDataAccessInterface userDAO;
    private final WashroomDataAccessInterface washroomDAO;
    private final HelpfulVoteDataAccessInterface helpfulVoteDAO;
    private final ReviewReportDataAccessInterface reportDAO;
    private final ViewReviewsOutputBoundary presenter;

    public SortReviewInteractor(ReviewDataAccessInterface reviewDAO,
                                UserDataAccessInterface userDAO,
                                WashroomDataAccessInterface washroomDAO,
                                HelpfulVoteDataAccessInterface helpfulVoteDAO,
                                ReviewReportDataAccessInterface reportDAO,
                                ViewReviewsOutputBoundary presenter) {
        this.reviewDAO = reviewDAO;
        this.userDAO = userDAO;
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

        String sortingOrder = inputData.sortBy();
        Comparator<entity.Review> comparator;
        switch(sortingOrder){
            case "Relevant":
                comparator = Comparator.comparing(SortReviewInteractor::score);
            case"Most Helpful":
                comparator = Comparator.comparing(entity.Review::getHelpfuls).reversed();
                break;
            case"Highest Rated":
                comparator = Comparator.comparing(entity.Review::getStars).reversed();
                break;
            case"Lowest Rated":
                comparator = Comparator.comparing(entity.Review::getStars);
                break;
            case"Newest":
                comparator = Comparator.comparing(entity.Review::createdAt).reversed();
                break;
            case"Voted by Me":
                if(userDAO.getCurrentUser().isPresent()){
                    comparator = Comparator.comparing((entity.Review review) ->
                            review.authorUsername().equals(userDAO.getCurrentUser().get().name())).reversed();
                } else {
                    comparator = Comparator.comparing(entity.Review::getHelpfuls);
                }
                break;
            case null, default:
                comparator = Comparator.comparing(SortReviewInteractor::score).reversed();
        }
        ArrayList<Review> sortedReviews = new ArrayList<>(reviewDAO.getReviewsForWashroom(inputData.currentWashroom()));
        sortedReviews.sort(comparator);
        ReviewSummary summary = reviewDAO.getSummary(washroom.id());
        List<ViewReviewsOutputData.ReviewDisplay> display = reviewDAO.getReviewsForWashroom(washroom.id()).stream()
                .sorted(comparator)
                .map(r -> new ViewReviewsOutputData.ReviewDisplay(
                        r.id(),
                        r.rating(),
                        r.comment(),
                        r.helpfulCount(),
                        r.createdAt(),
                        r.authorUsername(),
                        helpfulVoteDAO.hasVoted(r.id(), userDAO.getCurrentUser().toString()),
                        reportDAO.hasReported(r.id(), userDAO.getCurrentUser().toString())
                )).toList();
        presenter.present(new ViewReviewsOutputData(washroom.id(), washroom.building().name(), displayDescription(washroom),
                summary.averageRating(), summary.averageCleanliness(), summary.reviewCount(),
                washroom.numToilets(), washroom.numSinks(), display));
        }
    }
