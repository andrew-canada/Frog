package use_case.write_review;

public interface WriteReviewOutputBoundary {
    /**
     * Performs this operation.
     *
     * @param outputData parameter value.
     */
    void present(WriteReviewOutputData outputData);
}
