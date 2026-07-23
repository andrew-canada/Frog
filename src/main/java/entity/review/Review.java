package entity.review;

public interface Review {
    public int getStars();
    public String getText();
    public int getHelpfuls();
    public int getUnhelpfuls();
    public default int getHelpfulness() {return getHelpfuls() - getUnhelpfuls();}
}
