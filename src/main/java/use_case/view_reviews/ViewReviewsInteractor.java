package use_case.view_reviews;

import entity.Review;
import entity.ReviewSummary;
import entity.Washroom;
import data_access.washroom.WashroomDataAccessInterface;

import java.util.Comparator;
import java.util.List;

public final class ViewReviewsInteractor implements ViewReviewsInputBoundary {
    private final ReviewDataAccessInterface reviews;
    private final WashroomDataAccessInterface washrooms;
    private final ViewReviewsOutputBoundary presenter;

    public ViewReviewsInteractor(ReviewDataAccessInterface reviews, WashroomDataAccessInterface washrooms,
                                 ViewReviewsOutputBoundary presenter) {
        this.reviews = reviews;
        this.washrooms = washrooms;
        this.presenter = presenter;
    }

    @Override
    public void execute(ViewReviewsInputData input) {
        Washroom washroom = washrooms.getById(input.washroomId()).orElse(null);
        if (washroom == null) {
            presenter.presentError("Washroom not found");
            return;
        }
        ReviewSummary summary = reviews.getSummary(washroom.id());
        List<ViewReviewsOutputData.ReviewDisplay> display = reviews.getReviewsForWashroom(washroom.id()).stream()
                .sorted(Comparator.comparingInt(Review::helpfulCount).reversed())
                .map(r -> new ViewReviewsOutputData.ReviewDisplay(r.rating(), r.comment(), r.helpfulCount(),
                        r.createdAt(), r.authorUsername())).toList();
        presenter.present(new ViewReviewsOutputData(washroom.id(), washroom.name(),
                washroom.gender().name().replace('_', '-').toLowerCase() + (washroom.accessible() ? " · accessible" : ""),
                summary.averageRating(), summary.averageCleanliness(), summary.reviewCount(),
                washroom.numToilets(), washroom.numSinks(), display));
    }
}
