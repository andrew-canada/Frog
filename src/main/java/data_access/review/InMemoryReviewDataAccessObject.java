package data_access.review;

import entity.Review;
import entity.ReviewSummary;
import use_case.view_reviews.ReviewDataAccessInterface;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class InMemoryReviewDataAccessObject implements ReviewDataAccessInterface {
    private final List<Review> reviews = new ArrayList<>();

    public InMemoryReviewDataAccessObject() {
        reviews.add(new Review("r1", "bahen-2", "sheena_q", 5, 5,
                "Spotless and rarely busy. Good lighting and a spacious accessible stall.", 14, LocalDate.of(2026, 3, 12)));
        reviews.add(new Review("r2", "bahen-2", "andrew_p", 4, 4,
                "Clean most days but can get crowded between classes. Soap was full.", 6, LocalDate.of(2026, 2, 28)));
        reviews.add(new Review("r3", "robarts-4", "eleanor_l", 4, 4,
                "Reliable and easy to find, though busy after lunch.", 8, LocalDate.of(2026, 4, 3)));
        reviews.add(new Review("r4", "gerstein-main", "ian_c", 3.5, 3,
                "Quiet in the morning. One sink was out of service.", 4, LocalDate.of(2026, 4, 16)));
    }

    public InMemoryReviewDataAccessObject(List<Review> seed) { reviews.addAll(seed); }
    @Override public List<Review> getReviewsForWashroom(String id) {
        return reviews.stream().filter(r -> r.washroomId().equals(id)).toList();
    }
    @Override public ReviewSummary getSummary(String id) {
        List<Review> found = getReviewsForWashroom(id);
        if (found.isEmpty()) return ReviewSummary.empty();
        return new ReviewSummary(found.stream().mapToDouble(Review::rating).average().orElse(0),
                found.stream().mapToDouble(Review::cleanliness).average().orElse(0), found.size());
    }
    @Override public List<Review> getReviewsByUser(String username) {
        return reviews.stream().filter(r -> username.equals(r.authorUsername())).toList();
    }
}
