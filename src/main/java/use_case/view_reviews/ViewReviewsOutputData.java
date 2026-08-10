package use_case.view_reviews;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Review data prepared for the review screen.
 */
public final class ViewReviewsOutputData {
    private static final int VALUE_COUNT = 9;
    private static final int AVERAGE_RATING_INDEX = 3;
    private static final int AVERAGE_CLEANLINESS_INDEX = 4;
    private static final int REVIEW_COUNT_INDEX = 5;
    private static final int TOILETS_INDEX = 6;
    private static final int SINKS_INDEX = 7;
    private static final int REVIEWS_INDEX = 8;
    private final String washroomId;
    private final String washroomName;
    private final String subtitle;
    private final double averageRating;
    private final double averageCleanliness;
    private final int reviewCount;
    private final int numToilets;
    private final int numSinks;
    private final List<ReviewDisplay> reviews;

    public ViewReviewsOutputData(final Object... values) {
        if (values.length != VALUE_COUNT) {
            throw new IllegalArgumentException("ViewReviewsOutputData requires nine values");
        }
        this.washroomId = (String) values[0];
        this.washroomName = (String) values[1];
        this.subtitle = (String) values[2];
        this.averageRating = ((Number) values[AVERAGE_RATING_INDEX]).doubleValue();
        this.averageCleanliness = ((Number) values[AVERAGE_CLEANLINESS_INDEX]).doubleValue();
        this.reviewCount = ((Number) values[REVIEW_COUNT_INDEX]).intValue();
        this.numToilets = ((Number) values[TOILETS_INDEX]).intValue();
        this.numSinks = ((Number) values[SINKS_INDEX]).intValue();
        this.reviews = List.copyOf((List<ReviewDisplay>) values[REVIEWS_INDEX]);
    }

    /**
     * Returns the washroom identifier.
     *
     * @return the washroom identifier.
     */
    public String washroomId() {
        return washroomId;
    }

    /**
     * Returns the washroom name.
     *
     * @return the washroom name.
     */
    public String washroomName() {
        return washroomName;
    }

    /**
     * Returns the descriptive subtitle.
     *
     * @return the descriptive subtitle.
     */
    public String subtitle() {
        return subtitle;
    }

    /**
     * Returns the average rating.
     *
     * @return the average rating.
     */
    public double averageRating() {
        return averageRating;
    }

    /**
     * Returns the average cleanliness score.
     *
     * @return the average cleanliness score.
     */
    public double averageCleanliness() {
        return averageCleanliness;
    }

    /**
     * Returns the number of reviews.
     *
     * @return the number of reviews.
     */
    public int reviewCount() {
        return reviewCount;
    }

    /**
     * Returns the number of toilets.
     *
     * @return the number of toilets.
     */
    public int numToilets() {
        return numToilets;
    }

    /**
     * Returns the number of sinks.
     *
     * @return the number of sinks.
     */
    public int numSinks() {
        return numSinks;
    }

    /**
     * Returns the displayed reviews.
     *
     * @return the displayed reviews.
     */
    public List<ReviewDisplay> reviews() {
        return reviews;
    }

    public record ReviewDisplay(String reviewId, double rating, String comment, int helpfulCount, LocalDate date,
                                String author, boolean votedByCurrentUser, boolean reportedByCurrentUser) {
        public static final Comparator<ReviewDisplay> BY_HIGHEST_RATING = Comparator
            .comparing(ReviewDisplay::rating)
            .reversed();
        public static final Comparator<ReviewDisplay> BY_LOWEST_RATING = Comparator.comparing(ReviewDisplay::rating);
        public static final Comparator<ReviewDisplay> BY_HELPFULNESS = Comparator
            .comparing(ReviewDisplay::helpfulCount)
            .reversed();
        public static final Comparator<ReviewDisplay> BY_TIME_NEWEST = Comparator
            .comparing(ReviewDisplay::date)
            .reversed();
        public static final Comparator<ReviewDisplay> BY_ME = Comparator
            .comparing(ReviewDisplay::votedByCurrentUser)
            .reversed();
    }
}
