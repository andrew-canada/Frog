import org.junit.jupiter.api.Test;

/**
 * Lets Maven/Surefire execute the dependency-free tests used by TestRunner.
 */
final class JUnitBridgeTest {
    @Test
    void viewReviews() {
        ViewReviewsInteractorTest.run();
    }

    @Test
    void login() {
        LoginInteractorTest.run();
    }

    @Test
    void signup() {
        SignupInteractorTest.run();
    }

    @Test
    void statusReport() {
        SubmitStatusReportInteractorTest.run();
    }

    @Test
    void busyness() {
        BusynessStatsInteractorTest.run();
    }

    @Test
    void directions() {
        GetDirectionsInteractorTest.run();
    }

    @Test
    void graphhopperAdapter() {
        GraphhopperRouteDataAccessObjectTest.run();
    }

    @Test
    void graphhopperGeocodingAdapter() {
        GraphhopperGeocodingDataAccessObjectTest.run();
    }

    @Test
    void uiRendering() {
        UiSmokeTest.run();
    }

    @Test
    void voteHelpful() {
        VoteHelpfulInteractorTest.run();
    }

    @Test
    void reportReview() {
        ReportReviewInteractorTest.run();
    }

    @Test
    void moderateReviews() {
        ModerateReviewsInteractorTest.run();
    }

    @Test
    void sorting() {
        SortingInteractorTest.run();
    }

    @Test
    void personalPlan() {
        PersonalPlanInteractorTest.run();
    }

    @Test
    void filtering() {
        FilterInteractorTest.run();
    }

    @Test
    void changeUsername() {
        ChangeUsernameInteractorTest.run();
    }

    @Test
    void changePassword() {
        ChangePasswordInteractorTest.run();
    }

    @Test
    void deleteAccount() {
        DeleteAccountInteractorTest.run();
    }

    @Test
    void domainModels() {
        DomainModelTest.run();
    }
}
