package entity.washroom;

import entity.review.Review;

import java.util.ArrayList;
import java.util.List;

public class GenericWashroom {
    private final String floor;
    private final List<Review> reviews;

    public GenericWashroom(String floor) {
        this.floor = floor;
        this.reviews = new ArrayList<Review>();
    }

    public GenericWashroom(String floor, List<Review> reviews) {
        this.floor = floor;
        this.reviews = reviews;
    }

    public List<Review> getReviews() {
        return this.reviews;
    }

    public List<Review> getReviews(String sortBy) {
        if sortBy.equals()
        return this.reviews;
    }
}
