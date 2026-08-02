public final class TestRunner {
    public static void main(String[] args) {
        run("ViewReviewsInteractorTest", ViewReviewsInteractorTest::run);
        run("LoginInteractorTest", LoginInteractorTest::run);
        run("SignupInteractorTest", SignupInteractorTest::run);
        run("RecommendWashroomInteractorTest", RecommendWashroomInteractorTest::run);
        run("SubmitStatusReportInteractorTest", SubmitStatusReportInteractorTest::run);
        run("BusynessStatsInteractorTest", BusynessStatsInteractorTest::run);
        run("GetDirectionsInteractorTest", GetDirectionsInteractorTest::run);
        run("GraphhopperRouteDataAccessObjectTest", GraphhopperRouteDataAccessObjectTest::run);
        run("GraphhopperGeocodingDataAccessObjectTest", GraphhopperGeocodingDataAccessObjectTest::run);
        run("UiSmokeTest", UiSmokeTest::run);
        System.out.println("All 7 interactor tests, the GraphHopper adapter test, and the UI smoke test passed.");
    }

    private static void run(String name, Runnable test) {
        test.run();
        System.out.println("PASS  " + name);
    }
}
