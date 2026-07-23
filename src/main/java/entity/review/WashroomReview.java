package entity.review;

import java.util.Comparator;

class HelpfulnessComparator implements Comparator<Review>{
    @Override
    public int compare(Review r1, Review r2) {
        if(r1.getHelpfulness() > r2.getHelpfulness()) {
            return -1;
        } else if(r1.getHelpfulness() == r2.getHelpfulness()) {
            return 0;
        } else {
            return 1;
        }
    }
}

class StarsComparator implements Comparator<Review>{
    @Override
    public int compare(Review r1, Review r2) {
        if(r1.getStars() > r2.getStars()) {
            return -1;
        } else if(r1.getStars() == r2.getStars()) {
            return 0;
        } else {
            return 1;
        }
    }
}

public class WashroomReview implements Review{

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
    public int getStars() {return this.stars;}

    @Override
    public String getText(){return this.text;}

    @Override
    public int getHelpfuls(){return this.helpfuls;}

    @Override
    public int getUnhelpfuls() {return this.unhelpfuls;}

}
