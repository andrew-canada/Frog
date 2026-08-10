package interface_adapter.moderate_reviews;

import use_case.moderate_reviews.ModerateReviewsInputBoundary;
import use_case.moderate_reviews.ModerateReviewsInputData;

/**
 * Controller for the Moderator Remove Review use case.
 */
public final class ModerateReviewsController {

    private final ModerateReviewsInputBoundary interactor;

    public ModerateReviewsController(final ModerateReviewsInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Performs this operation.
     */
    public void load() {
        interactor.loadReportedReviews();
    }

    /**
     * Performs this operation.
     *
     * @param reviewId parameter value.
     *
     * @param moderatorUsername parameter value.
     */
    public void remove(final String reviewId, final String moderatorUsername) {
        interactor.removeReview(new ModerateReviewsInputData(reviewId, moderatorUsername));
    }

    /**
     * Performs this operation.
     *
     * @param reviewId parameter value.
     *
     * @param moderatorUsername parameter value.
     */
    public void dismiss(final String reviewId, final String moderatorUsername) {
        interactor.dismissReports(new ModerateReviewsInputData(reviewId, moderatorUsername));
    }
}
