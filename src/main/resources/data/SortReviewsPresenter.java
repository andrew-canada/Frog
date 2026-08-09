package data;

import interface_adapter.view_reviews.ReviewsViewModel;
import use_case.view_reviews.ViewReviewsOutputBoundary;
import use_case.view_reviews.ViewReviewsOutputData;

public class SortReviewsPresenter implements ViewReviewsOutputBoundary {
    private final ReviewsViewModel reviewsViewModel;

    public SortReviewsPresenter(ReviewsViewModel reviewsViewModel) {
        this.reviewsViewModel = reviewsViewModel;
    }
    private static String listDescription(String washroomName) {
        int separator = washroomName.indexOf('|');
        String description = separator >= 0 ? washroomName.substring(separator + 1) : washroomName;
        return description.replaceAll("(?i)\\bwashrooms?\\b", "")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    @Override
    public void present(ViewReviewsOutputData outputData) {
        reviewsViewModel.setState(new ReviewsViewModel.State(

                ));
    }
}
