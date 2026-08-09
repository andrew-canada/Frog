package use_case.port;

import java.util.List;

import entity.Review;
import entity.ReviewSummary;

public interface ReviewRepository {
    List<Review> getReviewsForWashroom(String washroomId);

    ReviewSummary getSummary(String washroomId);

    List<Review> getReviewsByUser(String username);

    void save(Review review);
}
