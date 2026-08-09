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

    public void load() {
        interactor.loadReportedReviews();
    }

    public void remove(final String reviewId, final String moderatorUsername) {
        interactor.removeReview(new ModerateReviewsInputData(reviewId, moderatorUsername));
    }

    public void dismiss(final String reviewId, final String moderatorUsername) {
        interactor.dismissReports(new ModerateReviewsInputData(reviewId, moderatorUsername));
    }
}
