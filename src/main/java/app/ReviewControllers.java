package app;

import interface_adapter.report_review.ReportReviewController;
import interface_adapter.report_review.ReportReviewPresenter;
import interface_adapter.view_reviews.ViewReviewsController;
import interface_adapter.view_reviews.ViewReviewsPresenter;
import interface_adapter.vote_helpful.VoteHelpfulController;
import interface_adapter.write_review.WriteReviewController;
import interface_adapter.write_review.WriteReviewPresenter;
import use_case.report_review.ReportReviewInteractor;
import use_case.view_reviews.ViewReviewsInteractor;
import use_case.vote_helpful.VoteHelpfulInteractor;
import use_case.write_review.WriteReviewInteractor;

/** Review controller construction. */
final class ReviewControllers {
    private final ViewReviewsController viewReviews;
    private final ViewReviewsPresenter viewReviewsPresenter;
    private final WriteReviewController writeReview;
    private final VoteHelpfulController voteHelpful;
    private final ReportReviewController reportReview;

    private ReviewControllers(final ViewReviewsController viewReviews, final ViewReviewsPresenter viewReviewsPresenter,
                              final WriteReviewController writeReview, final VoteHelpfulController voteHelpful,
                              final ReportReviewController reportReview) {
        this.viewReviews = viewReviews;
        this.viewReviewsPresenter = viewReviewsPresenter;
        this.writeReview = writeReview;
        this.voteHelpful = voteHelpful;
        this.reportReview = reportReview;
    }

    static ReviewControllers create(final ApplicationInfrastructure infrastructure, final ReviewModels models) {
        final ViewReviewsPresenter presenter = new ViewReviewsPresenter(models.reviews());
        return new ReviewControllers(
            new ViewReviewsController(new ViewReviewsInteractor(infrastructure.reviews(), infrastructure.washrooms(),
                infrastructure.reviews(), infrastructure.reviews(), presenter)), presenter,
            new WriteReviewController(new WriteReviewInteractor(infrastructure.reviews(),
                new WriteReviewPresenter(models.writeReview()))),
            new VoteHelpfulController(new VoteHelpfulInteractor(infrastructure.reviews())),
            new ReportReviewController(new ReportReviewInteractor(infrastructure.reviews(),
                new ReportReviewPresenter(models.reportReview()))));
    }

    ViewReviewsController viewReviews() {
        return viewReviews;
    }

    ViewReviewsPresenter viewReviewsPresenter() {
        return viewReviewsPresenter;
    }

    WriteReviewController writeReview() {
        return writeReview;
    }

    VoteHelpfulController voteHelpful() {
        return voteHelpful;
    }

    ReportReviewController reportReview() {
        return reportReview;
    }
}
