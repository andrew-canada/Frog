package entity.washroom;

import entity.building.Building;
import entity.review.Review;
import java.util.List;

public interface Washroom {

    public Building getBuilding();

    public List<Review> getReviews();
}
