package use_case.moderate_reviews;

/**
 * Input for a moderator action on a specific review.
 * @param moderatorUsername parameter value.
 * @param reviewId parameter value.
 */
public record ModerateReviewsInputData(String reviewId, String moderatorUsername) {
}
