package use_case.vote_helpful;

/**
 * Tracks which users have voted a review "helpful". Implemented by the review
 * data access objects (InMemory / Mongo).
 */
public interface HelpfulVoteDataAccessInterface {

    /**
     * @return whether the user has already voted the review helpful
     */
    boolean hasVoted(String reviewId, String username);

    /**
     * Records the user's helpful vote and increments the review's helpful count.
     */
    void addVote(String reviewId, String username);

    /**
     * Removes the user's helpful vote and decrements the review's helpful count.
     */
    void removeVote(String reviewId, String username);
}
