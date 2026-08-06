package use_case.moderate_reviews;

/** Input for a moderator action on a specific review. */
public record ModerateReviewsInputData(String reviewId, String moderatorUsername) { }
