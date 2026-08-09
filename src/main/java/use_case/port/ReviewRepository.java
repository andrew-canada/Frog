package use_case.port;

import entity.Review;
import entity.ReviewSummary;

import java.util.List;

public interface ReviewRepository {
    List<Review> getReviewsForWashroom(String washroomId);

    ReviewSummary getSummary(String washroomId);

    List<Review> getReviewsByUser(String username);

    void save(Review review);
}
