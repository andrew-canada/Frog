package entity.review;

public class WashroomReviewFactory implements ReviewFactory{

    @Override
    public WashroomReview create(int stars, String text) {
        return new WashroomReview(stars, text);
    }

    @Override
    public WashroomReview create(
            int stars,
            String text,
            int helpfuls,
            int unhelpfuls) {
        return new WashroomReview(stars, text, helpfuls, unhelpfuls);
    }
}
