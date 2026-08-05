package use_case.write_review;

public record WriteReviewInputData(String washroomId, String username, int rating, int cleanliness, String comment) { }
