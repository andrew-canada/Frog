package app;

import java.util.List;
import java.util.function.Consumer;

import entity.Washroom;
import views.LoginPanel;
import views.MainView;
import views.ReadReviewsView;

/** Main navigation views and their controller injection. */
final class PrimaryViews {
    private final MainView main;
    private final ReadReviewsView reviews;
    private final LoginPanel login;

    private PrimaryViews(final MainView main, final ReadReviewsView reviews, final LoginPanel login) {
        this.main = main;
        this.reviews = reviews;
        this.login = login;
    }

    static PrimaryViews create(final ApplicationInfrastructure infrastructure, final ApplicationModels models,
                               final ApplicationControllers controllers) {
        final MainView main = new MainView(models.features().list(), models.features().map(),
            models.features().filter(), models.auth().isLoggedIn(), controllers.auth().logout());
        main.setAddressLookup(infrastructure.geocoding()::lookup);
        main.setFilterController(controllers.navigation().filter());
        main.setSortWashroomController(controllers.navigation().sortWashroom());
        final ReadReviewsView reviews = new ReadReviewsView(models.reviews().reviews());
        reviews.setSortReviewsController(controllers.moderation().sortReviews());
        final LoginPanel login = new LoginPanel(models.auth().login(), controllers.auth().login());
        return new PrimaryViews(main, reviews, login);
    }

    MainView main() {
        return main;
    }

    ReadReviewsView reviews() {
        return reviews;
    }

    LoginPanel login() {
        return login;
    }

    void setMainOnReviews(final Consumer<String> action) {
        main.setOnReviews(action);
    }

    void setMainOnDirections(final Consumer<String> action) {
        main.setOnDirections(action);
    }

    void setMainOnLogin(final Runnable action) {
        main.setOnLogin(action);
    }

    void setMainOnLogout(final Runnable action) {
        main.setOnLogout(action);
    }

    void setMainOnAccount(final Runnable action) {
        main.setOnAccount(action);
    }

    void setMainOnReport(final Runnable action) {
        main.setOnReport(action);
    }

    void setMainOnBusyness(final Runnable action) {
        main.setOnBusyness(action);
    }

    void setMainOnModerator(final Runnable action) {
        main.setOnModerator(action);
    }

    void setReviewsOnBack(final Runnable action) {
        reviews.setOnBack(action);
    }

    void setReviewsOnWrite(final Runnable action) {
        reviews.setOnWrite(action);
    }

    void setReviewsOnHelpful(final Consumer<String> action) {
        reviews.setOnHelpful(action);
    }

    void setReviewsOnReport(final Consumer<String> action) {
        reviews.setOnReport(action);
    }

    void setLoginOnBack(final Runnable action) {
        login.setOnBack(action);
    }

    void setLoginOnSignup(final Runnable action) {
        login.setOnSignup(action);
    }

    void setWashrooms(final List<Washroom> washrooms) {
        main.setWashrooms(washrooms);
    }

    void setHeatmapData(final List<MainView.HeatmapData> heatmapData) {
        main.setHeatmapData(heatmapData);
    }

    void showRouting() {
        main.showRouting();
    }

    double latitude() {
        return main.latitude();
    }

    double longitude() {
        return main.longitude();
    }

    String selectedId() {
        return main.selectedId();
    }

    void setModerator(final boolean moderator) {
        main.setModerator(moderator);
    }

    void setModeratorReportCount(final int count) {
        main.setModeratorReportCount(count);
    }
}
