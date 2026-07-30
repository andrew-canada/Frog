package entity.review;

public interface ReviewFactory {
    public Review create(int stars, String text);

    public Review create(
            int stars,
            String text,
            int helpfuls,
            int unhelpfuls);
}
