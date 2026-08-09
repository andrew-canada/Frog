package interface_adapter.write_review;

import use_case.write_review.WriteReviewInputBoundary;
import use_case.write_review.WriteReviewInputData;

public final class WriteReviewController {
    private final WriteReviewInputBoundary interactor;

    public WriteReviewController(final WriteReviewInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(final String washroomId, final String username, final int rating, final int cleanliness, final String comment) {
        interactor.execute(new WriteReviewInputData(washroomId, username, rating, cleanliness, comment));
    }
}
