package interface_adapter.moderate_reviews;

import use_case.moderate_reviews.ModerateReviewsOutputBoundary;
import use_case.moderate_reviews.ModerateReviewsOutputData;

/** Presenter for the Moderator Remove Review use case. */
public final class ModerateReviewsPresenter implements ModerateReviewsOutputBoundary {

    private final ModerateReviewsViewModel model;

    public ModerateReviewsPresenter(ModerateReviewsViewModel model) {
        this.model = model;
    }

    @Override
    public void present(ModerateReviewsOutputData output) {
        model.setState(new ModerateReviewsViewModel.State(output.reportedReviews(), output.message()));
    }
}
