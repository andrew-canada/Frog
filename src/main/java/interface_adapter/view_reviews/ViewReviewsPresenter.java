package interface_adapter.view_reviews;

import use_case.view_reviews.ViewReviewsOutputBoundary;
import use_case.view_reviews.ViewReviewsOutputData;

public final class ViewReviewsPresenter implements ViewReviewsOutputBoundary {
    private final ReviewsViewModel model;

    public ViewReviewsPresenter(final ReviewsViewModel model) {
        this.model = model;
    }

    @Override
    public void present(final ViewReviewsOutputData d) {
        model.setState(new ReviewsViewModel.State(d.washroomId(), d.washroomName(), d.subtitle(), d.averageRating(),
            d.averageCleanliness(), d.reviewCount(), d.numToilets(), d.numSinks(), d.reviews(), null));
    }

    @Override
    public void presentError(final String message) {
        final ReviewsViewModel.State s = model.getState();
        model.setState(new ReviewsViewModel.State(s.washroomId(), s.name(), s.subtitle(), s.rating(), s.cleanliness(),
            s.reviewCount(), s.toilets(), s.sinks(), s.reviews(), message));
    }
}
