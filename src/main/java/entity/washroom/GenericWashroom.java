package entity.washroom;

import entity.review.Review;

import java.util.Comparator;
import java.util.List;

class HighestOverallComparator implements Comparator<Review> {
    @Override
    public int compare(Review o1, Review o2) {
        return Integer.compare(o2.getStars(), o1.getStars());
    }
}

public class GenericWashroom implements Washroom {
    private final String floor;

    public GenericWashroom(String floor) {
        this.floor = floor;
    }

    public GenericWashroom(String floor, List<Review> reviews) {
        this.floor = floor;
    }

    public String getFloor() {
        return floor;
    }
}
