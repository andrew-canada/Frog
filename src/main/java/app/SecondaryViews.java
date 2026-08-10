package app;

import java.util.function.Consumer;

import entity.MaintenanceIssue;
import views.AccountView;
import views.BusynessChartView;
import views.ReportedReviewsView;
import views.StatusReportView;

/** Secondary feature views and their controller injection. */
final class SecondaryViews {
    private final AccountView account;
    private final StatusReportView status;
    private final BusynessChartView busyness;
    private final ReportedReviewsView moderate;

    private SecondaryViews(final AccountView account, final StatusReportView status,
                           final BusynessChartView busyness, final ReportedReviewsView moderate) {
        this.account = account;
        this.status = status;
        this.busyness = busyness;
        this.moderate = moderate;
    }

    static SecondaryViews create(final ApplicationModels models, final ApplicationControllers controllers) {
        final AccountView account = new AccountView(models.features().account(),
            controllers.account().changeUsername(), controllers.account().changePassword(),
            controllers.account().deleteAccount(), controllers.personalPlan().personalPlan());
        final StatusReportView status = new StatusReportView(models.features().status());
        final BusynessChartView busyness = new BusynessChartView(models.features().busyness());
        final ReportedReviewsView moderate = new ReportedReviewsView(models.reviews().moderate());
        moderate.setController(controllers.moderation().moderate());
        return new SecondaryViews(account, status, busyness, moderate);
    }

    AccountView account() {
        return account;
    }

    StatusReportView status() {
        return status;
    }

    BusynessChartView busyness() {
        return busyness;
    }

    ReportedReviewsView moderate() {
        return moderate;
    }

    void setAccountOnBack(final Runnable action) {
        account.setOnBack(action);
    }

    void setAccountOnViewPlan(final Consumer<String> action) {
        account.setOnViewPlan(action);
    }

    void setStatusOnBack(final Runnable action) {
        status.setOnCancel(action);
    }

    void setStatusOnSubmit(final Runnable action) {
        status.setOnSubmit(action);
    }

    void setBusynessOnBack(final Runnable action) {
        busyness.setOnBack(action);
    }

    void setModerateOnBack(final Runnable action) {
        moderate.setOnBack(action);
    }

    void setModerateUsername(final String username) {
        moderate.setModeratorUsername(username);
    }

    void setStatusWashroomName(final String name) {
        status.setWashroomName(name);
    }

    void setBusynessLocationName(final String name) {
        busyness.setLocationName(name);
    }

    int statusBusyness() {
        return status.busyness();
    }

    int statusCleanliness() {
        return status.cleanliness();
    }

    MaintenanceIssue statusIssue() {
        return status.issue();
    }
}
