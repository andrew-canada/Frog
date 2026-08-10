package use_case.port;

import java.util.List;

import entity.Review;
import entity.ReviewSummary;

public interface ReviewRepository {
    /**
     * Performs this operation.
     *
     * @param washroomId parameter value.
     *
     * @return the operation result.
     */
    List<Review> getReviewsForWashroom(String washroomId);

    /**
     * Performs this operation.
     *
     * @param washroomId parameter value.
     *
     * @return the operation result.
     */
    ReviewSummary getSummary(String washroomId);

    /**
     * Performs this operation.
     *
     * @param username parameter value.
     *
     * @return the operation result.
     */
    List<Review> getReviewsByUser(String username);

    /**
     * Performs this operation.
     *
     * @param review parameter value.
     */
    void save(Review review);
}
