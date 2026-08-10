import entity.Building;
import entity.EnrollmentMeeting;
import entity.GeoPoint;
import entity.MaintenanceIssue;
import entity.Report;
import entity.Review;
import entity.ReviewSummary;
import entity.Route;
import entity.StatusReport;
import entity.User;
import entity.Washroom;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Exercises the validation and value semantics used throughout the use cases. */
final class DomainModelTest {
    private DomainModelTest() {
    }

    static void run() {
        buildingAndWashroomExposeTheirValuesAndRejectInvalidInput();
        reviewsAndSummariesValidateAndCalculateAverages();
        reportsAndRoutesDefensivelyCopyCollections();
        statusReportsAndUsersValidateTheirRequiredValues();
        simpleDomainValuesRetainTheirValues();
    }

    private static void buildingAndWashroomExposeTheirValuesAndRejectInvalidInput() {
        final Building building = new Building("BA", "Bahen", 43.66, -79.39);
        TestSupport.check(building.getBuildingCode().equals("BA")
                && building.getBuildingNameShort().equals("Bahen")
                && building.getBuildingNameLong().equals("Bahen")
                && building.getControlInfo().isEmpty()
                && building.getLatitude() == 43.66
                && building.getLongitude() == -79.39,
            "building compatibility accessors should retain all values");
        invalid(() -> new Building("", "Bahen", 0, 0), "building requires a code");
        invalid(() -> new Building("BA", " ", 0, 0), "building requires a name");

        final Washroom washroom = TestSupport.washroom();
        TestSupport.check(washroom.id().equals("w1") && washroom.name().equals("Test washroom")
                && washroom.building().equals(building) && washroom.floor().equals("2nd")
                && washroom.accessible() && washroom.gender() == Washroom.Gender.ALL_GENDER
                && washroom.numToilets() == 3 && washroom.numSinks() == 2
                && washroom.locationDescription().equals("By elevators"),
            "washroom should expose all constructor values");
        final ReviewSummary updated = new ReviewSummary(3, 2, 1);
        washroom.updateReviewSummary(updated);
        TestSupport.check(washroom.reviewSummary().equals(updated), "washroom summary updates");
        invalid(() -> new Washroom("only"), "washroom needs ten values");
        invalid(() -> new Washroom("", "n", building, "1", true, Washroom.Gender.MEN, 0, 0, "l",
            ReviewSummary.empty()), "washroom requires an id");
        invalid(() -> new Washroom("id", "n", building, "1", true, Washroom.Gender.MEN, -1, 0, "l",
            ReviewSummary.empty()), "washroom rejects negative toilet counts");
        invalid(() -> washroom.updateReviewSummary(null), "washroom rejects a null summary");
    }

    private static void reviewsAndSummariesValidateAndCalculateAverages() {
        final Review first = new Review("r1", "w1", "u", 4.4, 3.5, "Useful", 2, LocalDate.now());
        final Review second = new Review("r2", "w1", "u", 1, 5, "", 0, LocalDate.now());
        TestSupport.check(first.getStars() == 4 && first.getText().equals("Useful")
                && first.getHelpfuls() == 2 && first.getUnhelpfuls() == 0,
            "review compatibility values should be derived correctly");
        final ReviewSummary summary = ReviewSummary.fromReviews(List.of(first, second));
        TestSupport.check(summary.reviewCount() == 2 && summary.averageRating() == 2.7
                && summary.averageCleanliness() == 4.25 && ReviewSummary.empty().reviewCount() == 0,
            "review summaries should calculate averages and empty values");
        invalid(() -> new Review("r", "w", "u", 0, 1, "", 0, LocalDate.now()), "rating lower bound");
        invalid(() -> new Review("r", "w", "u", 1, 6, "", 0, LocalDate.now()), "cleanliness upper bound");
        invalid(() -> new Review("r", "w", "u", 1, 1, "", -1, LocalDate.now()), "helpful count lower bound");
        invalid(() -> new ReviewSummary(0, 0, -1), "review count lower bound");
    }

    private static void reportsAndRoutesDefensivelyCopyCollections() {
        final List<String> reasons = new ArrayList<>(List.of("Spam"));
        final Report report = new Report("report", "review", "user", reasons, "details", LocalDateTime.now());
        reasons.add("Other");
        TestSupport.check(report.reasons().equals(List.of("Spam")), "reports copy their reasons");
        invalid(() -> report.reasons().add("Other"), "report reasons are immutable");

        final List<GeoPoint> points = new ArrayList<>(List.of(new GeoPoint(1, 2)));
        final Route route = new Route(points, 25, 30);
        points.add(new GeoPoint(3, 4));
        TestSupport.check(route.points().size() == 1 && route.distanceMeters() == 25 && route.timeSeconds() == 30,
            "routes copy their points and retain measurements");
        invalid(() -> route.points().add(new GeoPoint(3, 4)), "route points are immutable");
    }

    private static void statusReportsAndUsersValidateTheirRequiredValues() {
        final StatusReport status = new StatusReport("w1", "user", 5, 1, MaintenanceIssue.CLOGGED,
            LocalDateTime.now());
        TestSupport.check(status.busyness() == 5 && status.cleanliness() == 1
                && status.issue().toString().equals("Clogged"),
            "status reports retain ratings and issue labels");
        invalid(() -> new StatusReport("", "u", 1, 1, MaintenanceIssue.NONE, LocalDateTime.now()),
            "status report requires a washroom");
        invalid(() -> new StatusReport("w", "u", 0, 1, MaintenanceIssue.NONE, LocalDateTime.now()),
            "status report validates rating bounds");
        invalid(() -> new StatusReport("w", "u", 1, 1, null, LocalDateTime.now()),
            "status report requires an issue");
        invalid(() -> new StatusReport("w", "u", 1, 1, MaintenanceIssue.NONE, null),
            "status report requires a timestamp");

        final User ordinary = new User("user", "hash", "plan");
        final User moderator = new User("mod", "hash", "", true);
        TestSupport.check(ordinary.name().equals("user") && !ordinary.isModerator() && moderator.isModerator(),
            "user constructors should set moderator status");
        invalid(() -> new User("", "hash", ""), "user requires a username");
        invalid(() -> new User("user", "", ""), "user requires a password hash");
    }

    private static void simpleDomainValuesRetainTheirValues() {
        final EnrollmentMeeting meeting = new EnrollmentMeeting(9, 10, 30);
        final GeoPoint point = new GeoPoint(43.6, -79.4);
        TestSupport.check(meeting.startHour() == 9 && meeting.endHour() == 10 && meeting.enrollment() == 30
                && point.latitude() == 43.6 && point.longitude() == -79.4,
            "simple domain records retain constructor values");
        for (final MaintenanceIssue issue : MaintenanceIssue.values()) {
            TestSupport.check(!issue.toString().isBlank(), "every maintenance issue has a display label");
        }
    }

    private static void invalid(final Runnable action, final String message) {
        try {
            action.run();
            throw new AssertionError(message);
        }
        catch (final IllegalArgumentException | UnsupportedOperationException | NullPointerException expected) {
            // Expected validation or defensive-copy failure.
        }
    }
}
