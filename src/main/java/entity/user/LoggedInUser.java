package entity.user;

import entity.review.Review;
import java.util.ArrayList;
import java.util.List;

public class LoggedInUser implements User{

    private final String name;
    private final String password;
    private final String personalPlan;
    private List<Review> reviews;

    public LoggedInUser(String name, String password) {
        this(name, password, List.of(), "");
    }

    public LoggedInUser(String name, String password, List<Review> reviews) {
        this(name, password, reviews, "");
    }

    public LoggedInUser(String name, String password, List<Review> reviews, String personalPlan) {
        this.name = name;
        this.password = password;
        this.reviews = new ArrayList<>(reviews);
        this.personalPlan = personalPlan == null ? "" : personalPlan;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public String getPersonalPlan() { return personalPlan; }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

}
