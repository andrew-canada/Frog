package entity.review;

import java.util.Comparator;


public class WashroomReview implements Review {

    private int stars;
    private String text;
    private int helpfuls;
    private int unhelpfuls;

    public WashroomReview(int stars, String text) {
        this.stars = stars;
        this.text = text;
    }

    public WashroomReview(
            int stars,
            String text,
            int helpfuls,
            int unhelpfuls) {
        this.stars = stars;
        this.text = text;
        this.helpfuls = helpfuls;
        this.unhelpfuls = unhelpfuls;
    }

    @Override
    public int getStars() {
        return this.stars;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public int getHelpfuls() {
        return this.helpfuls;
    }

    @Override
    public int getUnhelpfuls() {
        return this.unhelpfuls;
    }

}
