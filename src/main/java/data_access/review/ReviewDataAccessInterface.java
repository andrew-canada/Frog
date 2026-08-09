package data_access.review;

import entity.Review;
import entity.ReviewSummary;

import java.util.List;

public interface ReviewDataAccessInterface {
    List<Review> getReviewsForWashroom(String washroomId);

    ReviewSummary getSummary(String washroomId);

    List<Review> getReviewsByUser(String username);

    default void save(Review review) {
        throw new UnsupportedOperationException("This review repository is read-only.");
    }
}
