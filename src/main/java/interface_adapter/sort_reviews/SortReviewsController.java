package interface_adapter.sort_reviews;

import java.util.concurrent.CompletableFuture;
import use_case.sort_review.ReviewSortOrder;
import use_case.sort_review.SortReviewInputBoundary;
import use_case.sort_review.SortReviewInputData;

public final class SortReviewsController {
    private final SortReviewInputBoundary interactor;

    public SortReviewsController(final SortReviewInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(final String sortBy, final String currentWashroom) {
        final SortReviewInputData inputData =
            new SortReviewInputData(ReviewSortOrder.fromDisplayLabel(sortBy), currentWashroom);
        CompletableFuture.runAsync(() -> interactor.execute(inputData));

    }
}
