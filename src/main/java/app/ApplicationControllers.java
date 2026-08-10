package app;

import java.time.DayOfWeek;
import java.util.concurrent.CompletableFuture;

import entity.MaintenanceIssue;

/** All use-case controllers assembled from the application's resources and state. */
final class ApplicationControllers {
    private final AuthControllers auth;
    private final AccountControllers account;
    private final PersonalPlanControllers personalPlan;
    private final ReviewControllers reviews;
    private final ModerationControllers moderation;
    private final NavigationControllers navigation;
    private final ReportControllers reports;

    private ApplicationControllers(final AuthControllers auth, final AccountControllers account,
                                   final PersonalPlanControllers personalPlan, final ReviewControllers reviews,
                                   final ModerationControllers moderation, final NavigationControllers navigation,
                                   final ReportControllers reports) {
        this.auth = auth;
        this.account = account;
        this.personalPlan = personalPlan;
        this.reviews = reviews;
        this.moderation = moderation;
        this.navigation = navigation;
        this.reports = reports;
    }

    static ApplicationControllers create(final ApplicationInfrastructure infrastructure,
                                         final ApplicationModels models) {
        final AuthControllers auth = AuthControllers.create(infrastructure, models.auth());
        final ReviewControllers reviews = ReviewControllers.create(infrastructure, models.reviews());
        return new ApplicationControllers(auth,
            AccountControllers.create(infrastructure, models.auth(), models.features()),
            PersonalPlanControllers.create(infrastructure, models.features()), reviews,
            ModerationControllers.create(infrastructure, models.reviews(), models.features(), reviews),
            NavigationControllers.create(infrastructure, models.features()),
            ReportControllers.create(infrastructure, models.features()));
    }

    AuthControllers auth() {
        return auth;
    }

    AccountControllers account() {
        return account;
    }

    PersonalPlanControllers personalPlan() {
        return personalPlan;
    }

    ReviewControllers reviews() {
        return reviews;
    }

    ModerationControllers moderation() {
        return moderation;
    }

    NavigationControllers navigation() {
        return navigation;
    }

    ReportControllers reports() {
        return reports;
    }

    void viewReviews(final String washroomId, final String username) {
        reviews.viewReviews().execute(washroomId, username);
    }

    void loadModeration() {
        moderation.moderate().load();
    }

    void toggleHelpful(final String reviewId, final String username) {
        reviews.voteHelpful().toggle(reviewId, username);
    }

    void requestDirections(final double latitude, final double longitude, final String washroomId) {
        CompletableFuture.runAsync(() -> {
            navigation.directions().execute(latitude, longitude, washroomId);
        });
    }

    void submitStatus(final String washroomId, final int busyness, final int cleanliness,
                      final MaintenanceIssue issue, final String username) {
        reports.status().execute(washroomId, busyness, cleanliness, issue, username);
    }

    void loadBusyness(final String washroomId, final String building, final DayOfWeek day) {
        reports.busyness().execute(washroomId, building, day);
    }
}
