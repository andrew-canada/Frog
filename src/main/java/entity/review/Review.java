package entity.review;

public interface Review {

    // these are just the methods we have agreed on; if more are needed, don't be afraid to add -mdmclbkairmnas
    int getStars();

    String getText();

    int getHelpfuls();

    int getUnhelpfuls();

    default int getOverallScore() {
        return getHelpfuls() - getUnhelpfuls();
    }
}
