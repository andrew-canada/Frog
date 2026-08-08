package entity.review;

public interface ReviewFactory {
    Review create(int stars, String text);

    Review create(
            int stars,
            String text,
            int helpfuls,
            int unhelpfuls);
}
