package use_case.moderate_reviews;

import entity.Review;
import java.util.Optional;

/**
 * Review lookups and mutations needed by moderation (looking a review up by id
 * and removing it).
 */
public interface ReviewAdminDataAccessInterface {

    /** @return the review with the given id, if it exists. */
    Optional<Review> getById(String reviewId);

    /** Permanently removes the review with the given id. */
    void deleteReview(String reviewId);
}
