package entity.user;

import entity.review.Review;
import java.util.ArrayList;
import java.util.List;

public class LoggedInUser implements User{

    private final String name;
    private final String password;
    private List<Review> reviews;

    public LoggedInUser(String name, String password) {
        this.name = name;
        this.password = password;
        this.reviews = new ArrayList<>();
    }

    public LoggedInUser(String name, String password, List<Review> reviews) {
        this.name = name;
        this.password = password;
        this.reviews = new ArrayList<>(reviews);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

}
