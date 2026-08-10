package use_case.vote_helpful;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tracks which users have voted a review "helpful". Implemented by the review
 * data access objects (InMemory / Mongo).
 */
public interface HelpfulVoteDataAccessInterface {

    /**
     * Checks whether the user has voted a review helpful.
     * @param reviewId parameter value.
     * @param username parameter value.
     * @return whether the user has already voted the review helpful.
     */
    boolean hasVoted(String reviewId, String username);

    /**
     * Returns the requested reviews already voted by the user, preferably in one query.
     * @param reviewIds parameter value.
     * @param username parameter value.
     * @return the operation result.
     */
    default Set<String> votedReviewIds(final Collection<String> reviewIds, final String username) {
        return reviewIds
            .stream()
            .filter(reviewId -> {
                return hasVoted(reviewId, username);
            })
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Records the user's helpful vote and increments the review's helpful count.
     * @param reviewId parameter value.
     * @param username parameter value.
     */
    void addVote(String reviewId, String username);

    /**
     * Removes the user's helpful vote and decrements the review's helpful count.
     * @param reviewId parameter value.
     * @param username parameter value.
     */
    void removeVote(String reviewId, String username);
}
