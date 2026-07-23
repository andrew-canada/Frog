package use_case.view_reviews;

import entity.Review;
import entity.ReviewSummary;
import java.util.List;

public interface ReviewDataAccessInterface {
    List<Review> getReviewsForWashroom(String washroomId);
    ReviewSummary getSummary(String washroomId);
    List<Review> getReviewsByUser(String username);
}
