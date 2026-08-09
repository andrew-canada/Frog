package interface_adapter.moderate_reviews;

import use_case.moderate_reviews.ModerateReviewsOutputBoundary;
import use_case.moderate_reviews.ModerateReviewsOutputData;

/**
 * Presenter for the Moderator Remove Review use case.
 */
public final class ModerateReviewsPresenter implements ModerateReviewsOutputBoundary {

    private final ModerateReviewsViewModel model;

    public ModerateReviewsPresenter(final ModerateReviewsViewModel model) {
        this.model = model;
    }

    @Override
    public void present(final ModerateReviewsOutputData output) {
        model.setState(new ModerateReviewsViewModel.State(output.reportedReviews(), output.message()));
    }
}
