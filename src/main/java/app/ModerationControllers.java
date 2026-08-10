package app;

import interface_adapter.moderate_reviews.ModerateReviewsController;
import interface_adapter.moderate_reviews.ModerateReviewsPresenter;
import interface_adapter.sort_reviews.SortReviewsController;
import use_case.moderate_reviews.ModerateReviewsInteractor;
import use_case.sort_review.SortReviewInteractor;

/** Moderation and review-sort controller construction. */
final class ModerationControllers {
    private final ModerateReviewsController moderate;
    private final SortReviewsController sortReviews;

    private ModerationControllers(final ModerateReviewsController moderate, final SortReviewsController sortReviews) {
        this.moderate = moderate;
        this.sortReviews = sortReviews;
    }

    static ModerationControllers create(final ApplicationInfrastructure infrastructure, final ReviewModels reviews,
                                        final FeatureModels features, final ReviewControllers reviewControllers) {
        final ModerateReviewsPresenter presenter = new ModerateReviewsPresenter(reviews.moderate());
        return new ModerationControllers(
            new ModerateReviewsController(new ModerateReviewsInteractor(infrastructure.reviews(),
                infrastructure.reviews(), infrastructure.washrooms(), infrastructure.users(), presenter)),
            new SortReviewsController(new SortReviewInteractor(infrastructure.reviews(), infrastructure.users(),
                infrastructure.washrooms(), infrastructure.reviews(), infrastructure.reviews(),
                reviewControllers.viewReviewsPresenter())));
    }

    ModerateReviewsController moderate() {
        return moderate;
    }

    SortReviewsController sortReviews() {
        return sortReviews;
    }
}
