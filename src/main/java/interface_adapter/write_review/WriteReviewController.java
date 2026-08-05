package interface_adapter.write_review;

import use_case.write_review.WriteReviewInputBoundary;
import use_case.write_review.WriteReviewInputData;

public final class WriteReviewController {
    private final WriteReviewInputBoundary interactor;
    public WriteReviewController(WriteReviewInputBoundary interactor) { this.interactor = interactor; }
    public void execute(String washroomId, String username, int rating, int cleanliness, String comment) {
        interactor.execute(new WriteReviewInputData(washroomId, username, rating, cleanliness, comment));
    }
}
