package interface_adapter.view_reviews;

import use_case.view_reviews.ViewReviewsInputBoundary;
import use_case.view_reviews.ViewReviewsInputData;

public final class ViewReviewsController {
    private final ViewReviewsInputBoundary interactor;

    public ViewReviewsController(final ViewReviewsInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(final String washroomId, final String username) {
        interactor.execute(new ViewReviewsInputData(washroomId, username));
    }
}
