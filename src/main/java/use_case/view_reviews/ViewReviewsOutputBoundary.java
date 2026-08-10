package use_case.view_reviews;

public interface ViewReviewsOutputBoundary {
    /**
     * Performs this operation.
     *
     * @param outputData parameter value.
     */
    void present(ViewReviewsOutputData outputData);

    /**
     * Performs this operation.
     *
     * @param message parameter value.
     */
    void presentError(String message);
}
