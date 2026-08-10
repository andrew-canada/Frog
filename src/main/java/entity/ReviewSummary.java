package entity;

import java.util.List;

public record ReviewSummary(double averageRating, double averageCleanliness, int reviewCount) {
    public ReviewSummary {
        if (reviewCount < 0) {
            throw new IllegalArgumentException("Review count cannot be negative");
        }
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public static ReviewSummary empty() {
        return new ReviewSummary(0, 0, 0);
    }

    /**
     * Builds a summary from reviews that were already loaded for a screen.
     */
    public static ReviewSummary fromReviews(final List<Review> reviews) {
        return new ReviewSummary(reviews
            .stream()
            .mapToDouble(Review::rating)
            .average()
            .orElse(0), reviews
            .stream()
            .mapToDouble(Review::cleanliness)
            .average()
            .orElse(0), reviews.size());
    }
}
