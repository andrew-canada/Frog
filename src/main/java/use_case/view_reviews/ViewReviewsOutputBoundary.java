package use_case.view_reviews;

public interface ViewReviewsOutputBoundary {
    void present(ViewReviewsOutputData outputData);

    void presentError(String message);
}
