package use_case.view_reviews;

import java.time.LocalDate;
import java.util.List;

public record ViewReviewsOutputData(String washroomId, String washroomName, String subtitle,
        double averageRating, double averageCleanliness, int reviewCount, int numToilets,
        int numSinks, List<ReviewDisplay> reviews) {
    public ViewReviewsOutputData { reviews = List.copyOf(reviews); }
    public record ReviewDisplay(double rating, String comment, int helpfulCount, LocalDate date, String author) { }
}
