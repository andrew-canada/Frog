package entity.user;

import entity.review.Review;

import java.util.ArrayList;
import java.util.List;

public class LoggedInUser implements User {

    private final String name;
    private final String password;
    private final String personalPlan;
    private final boolean moderator;
    private final List<Review> reviews;

    public LoggedInUser(String name, String password) {
        this(name, password, List.of(), "", false);
    }

    public LoggedInUser(String name, String password, List<Review> reviews) {
        this(name, password, reviews, "", false);
    }

    public LoggedInUser(String name, String password, List<Review> reviews, String personalPlan) {
        this(name, password, reviews, personalPlan, false);
    }

    public LoggedInUser(String name, String password, List<Review> reviews, String personalPlan, boolean moderator) {
        this.name = name;
        this.password = password;
        this.reviews = new ArrayList<>(reviews);
        this.personalPlan = personalPlan == null ? "" : personalPlan;
        this.moderator = moderator;
    }

    @Override
    public String name() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public String getPersonalPlan() {
        return personalPlan;
    }

    public boolean isModerator() {
        return moderator;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

}
