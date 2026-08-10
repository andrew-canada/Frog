package use_case.vote_helpful;

/**
 * Input for toggling a user's helpful vote on a review.
 * @param reviewId parameter value.
 * @param username parameter value.
 */
public record VoteHelpfulInputData(String reviewId, String username) {
}
