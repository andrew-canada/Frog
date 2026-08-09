package interface_adapter.sort_reviews;

import use_case.sort_review.SortReviewInputBoundary;
import use_case.sort_review.SortReviewInputData;
import use_case.sort_review.ReviewSortOrder;

import java.util.concurrent.CompletableFuture;

public final class SortReviewsController {
    private final SortReviewInputBoundary interactor;

    public SortReviewsController(SortReviewInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String sortBy, String currentWashroom) {
        SortReviewInputData inputData = new SortReviewInputData(ReviewSortOrder.fromDisplayLabel(sortBy), currentWashroom);
        CompletableFuture.runAsync(() -> interactor.execute(inputData));

    }
}
