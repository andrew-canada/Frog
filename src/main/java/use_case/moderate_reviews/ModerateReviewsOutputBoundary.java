package use_case.moderate_reviews;

/** The output boundary for the Moderator Remove Review use case. */
public interface ModerateReviewsOutputBoundary {

    /**
     * Presents the reported-review queue (after a load, remove, or dismiss).
     * @param output the queue and an optional message
     */
    void present(ModerateReviewsOutputData output);
}
