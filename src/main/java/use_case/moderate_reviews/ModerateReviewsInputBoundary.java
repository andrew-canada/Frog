package use_case.moderate_reviews;

/**
 * The input boundary for the Moderator Remove Review use case.
 */
public interface ModerateReviewsInputBoundary {

    /**
     * Loads the current queue of reported reviews.
     */
    void loadReportedReviews();

    /**
     * Removes a review (and its reports).
     * @param input parameter value.
     */
    void removeReview(ModerateReviewsInputData input);

    /**
     * Dismisses the reports on a review, leaving the review in place.
     * @param input parameter value.
     */
    void dismissReports(ModerateReviewsInputData input);
}
