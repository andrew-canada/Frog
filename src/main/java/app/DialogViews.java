package app;

import java.awt.Window;

import interface_adapter.report_review.ReportReviewViewModel;
import interface_adapter.write_review.WriteReviewViewModel;
import views.PersonalPlanView;
import views.ReportReviewDialog;
import views.SignupDialog;
import views.WriteReviewDialog;

/** Dialog construction kept separate from the window assembly. */
final class DialogViews {
    private final ApplicationControllers controllers;
    private final ApplicationModels models;

    private DialogViews(final ApplicationControllers controllers, final ApplicationModels models) {
        this.controllers = controllers;
        this.models = models;
    }

    static DialogViews create(final ApplicationControllers controllers, final ApplicationModels models) {
        return new DialogViews(controllers, models);
    }

    void showSignup(final Window owner) {
        new SignupDialog(owner, controllers.auth().signup()).setVisible(true);
    }

    void showWriteReview(final Window owner, final String washroomId, final String washroomName,
                         final String username, final Runnable onComplete) {
        final WriteReviewViewModel model = models.reviews().writeReview();
        final WriteReviewDialog dialog = new WriteReviewDialog(owner, model, controllers.reviews().writeReview(),
            washroomId, washroomName, username, onComplete);
        dialog.setVisible(true);
    }

    void showReportReview(final Window owner, final String reviewId, final String username) {
        final ReportReviewViewModel model = models.reviews().reportReview();
        new ReportReviewDialog(owner, controllers.reviews().reportReview(), model, reviewId, username)
            .setVisible(true);
    }

    void showPlan(final Window owner, final String plan) {
        new PersonalPlanView(owner, plan);
    }
}
