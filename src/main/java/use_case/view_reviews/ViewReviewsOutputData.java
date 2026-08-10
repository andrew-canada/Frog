package use_case.view_reviews;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record ViewReviewsOutputData(String washroomId, String washroomName, String subtitle, double averageRating,
                                    double averageCleanliness, int reviewCount, int numToilets, int numSinks,
                                    List<ReviewDisplay> reviews) {
    public ViewReviewsOutputData {
        reviews = List.copyOf(reviews);
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
