package entity;

public record ReviewSummary(double averageRating, double averageCleanliness, int reviewCount) {
    public ReviewSummary {
        if (reviewCount < 0) throw new IllegalArgumentException("Review count cannot be negative");
    }

    public static ReviewSummary empty() { return new ReviewSummary(0, 0, 0); }
}
