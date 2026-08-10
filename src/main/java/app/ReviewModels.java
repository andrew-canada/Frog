package app;

import interface_adapter.moderate_reviews.ModerateReviewsViewModel;
import interface_adapter.report_review.ReportReviewViewModel;
import interface_adapter.view_reviews.ReviewsViewModel;
import interface_adapter.write_review.WriteReviewViewModel;

/** Review-related state shared by controllers and views. */
final class ReviewModels {
    private final ReviewsViewModel reviews = new ReviewsViewModel();
    private final WriteReviewViewModel writeReview = new WriteReviewViewModel();
    private final ReportReviewViewModel reportReview = new ReportReviewViewModel();
    private final ModerateReviewsViewModel moderate = new ModerateReviewsViewModel();

    ReviewsViewModel reviews() {
        return reviews;
    }

    WriteReviewViewModel writeReview() {
        return writeReview;
    }

    ReportReviewViewModel reportReview() {
        return reportReview;
    }

    ModerateReviewsViewModel moderate() {
        return moderate;
    }
}
