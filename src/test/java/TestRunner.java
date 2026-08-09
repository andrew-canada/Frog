public final class TestRunner {
    public static void main(String[] args) {
        run("ViewReviewsInteractorTest", ViewReviewsInteractorTest::run);
        run("LoginInteractorTest", LoginInteractorTest::run);
        run("SignupInteractorTest", SignupInteractorTest::run);
        run("SubmitStatusReportInteractorTest", SubmitStatusReportInteractorTest::run);
        run("BusynessStatsInteractorTest", BusynessStatsInteractorTest::run);
        run("GetDirectionsInteractorTest", GetDirectionsInteractorTest::run);
        run("GraphhopperRouteDataAccessObjectTest", GraphhopperRouteDataAccessObjectTest::run);
        run("GraphhopperGeocodingDataAccessObjectTest", GraphhopperGeocodingDataAccessObjectTest::run);
        run("UiSmokeTest", UiSmokeTest::run);
        run("FilterInteractorTest", FilterInteractorTest::run);
        run("VoteHelpfulInteractorTest", VoteHelpfulInteractorTest::run);
        run("ReportReviewInteractorTest", ReportReviewInteractorTest::run);
        run("ModerateReviewsInteractorTest", ModerateReviewsInteractorTest::run);
        run("SortingInteractorTest", SortingInteractorTest::run);
        run("PersonalPlanInteractorTest", PersonalPlanInteractorTest::run);
        System.out.println("All interactor, adapter, sorting, and UI smoke tests passed.");
    }

    private static void run(String name, Runnable test) {
        test.run();
        System.out.println("PASS  " + name);
    }
}
