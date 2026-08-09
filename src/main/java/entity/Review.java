package entity;

import java.time.LocalDate;

public record Review(String id, String washroomId, String authorUsername, double rating,
                     double cleanliness, String comment, int helpfulCount,
                     LocalDate createdAt) {
    public Review {
        if (rating < 1 || rating > 5 || cleanliness < 1 || cleanliness > 5) {
            throw new IllegalArgumentException("Ratings must be from 1 to 5");
        }
        if (helpfulCount < 0) throw new IllegalArgumentException("Helpful count cannot be negative");
    }

    public int getStars() {
        return (int) Math.round(rating);
    }

    public String getText() {
        return comment;
    }

    public int getHelpfuls() {
        return helpfulCount;
    }

    public int getUnhelpfuls() {
        return 0;
    }
}
