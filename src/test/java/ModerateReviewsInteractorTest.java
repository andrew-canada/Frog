import entity.Report;
import entity.Review;
import entity.Washroom;
import data_access.washroom.WashroomDataAccessInterface;
import use_case.moderate_reviews.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

final class ModerateReviewsInteractorTest {

    static void run() {
        moderatorRemoveDeletesReviewAndReports();
        moderatorDismissClearsReportsKeepsReview();
        moderatorQueueSortsByReportsAndResolvesWashroomName();
        moderatorQueueSkipsReportsForMissingReview();
        moderatorActionRequiresModeratorPrivilege();
    }

    private static void moderatorRemoveDeletesReviewAndReports() {
        final List<Report> reports = new ArrayList<>(List.of(
                report("r1", "user2", List.of("Spam", "Other"), "wrong washroom"),
                report("r1", "user3", List.of("Harassment"), "")));
        final Set<String> deletedReviews = new HashSet<>();
        ReviewAdminDataAccessInterface reviewStore = reviewStore(deletedReviews);
        final ModerateReviewsOutputData[] out = new ModerateReviewsOutputData[1];
        ModerateReviewsInputBoundary moderator =
                new ModerateReviewsInteractor(reportStore(reports), reviewStore, noWashrooms(), moderators(true), d -> out[0] = d);

        moderator.loadReportedReviews();
        TestSupport.check(out[0].reportedReviews().size() == 1, "one reported review in the queue");
        ReportedReview reported = out[0].reportedReviews().get(0);
        TestSupport.check(reported.totalReports() == 3, "three reason-citations counted (Spam + Other + Harassment)");
        TestSupport.check(reported.additionalDetailsCount() == 1, "one report carried a written detail");

        moderator.removeReview(new ModerateReviewsInputData("r1", "moderator"));
        TestSupport.check(reviewStore.getById("r1").isEmpty(), "review deleted");
        TestSupport.check(reports.isEmpty(), "the review's reports cleared");
        TestSupport.check(out[0].reportedReviews().isEmpty(), "queue empty after removal");
    }

    private static void moderatorDismissClearsReportsKeepsReview() {
        final List<Report> reports = new ArrayList<>(List.of(report("r1", "user2", List.of("Spam"), "")));
        final Set<String> deletedReviews = new HashSet<>();
        final ModerateReviewsOutputData[] out = new ModerateReviewsOutputData[1];
        ModerateReviewsInputBoundary moderator =
                new ModerateReviewsInteractor(reportStore(reports), reviewStore(deletedReviews), noWashrooms(), moderators(true), d -> out[0] = d);

        moderator.dismissReports(new ModerateReviewsInputData("r1", "moderator"));
        TestSupport.check(reports.isEmpty(), "reports cleared on dismiss");
        TestSupport.check(deletedReviews.isEmpty(), "the review itself is kept on dismiss");
        TestSupport.check(out[0].reportedReviews().isEmpty(), "queue empty after dismiss");
    }

    private static void moderatorQueueSortsByReportsAndResolvesWashroomName() {
        // r1 gets two reports, r2 gets one -> r1 should sort ahead of r2.
        final List<Report> reports = new ArrayList<>(List.of(
                report("r1", "user2", List.of("Spam"), ""),
                report("r1", "user3", List.of("Harassment"), ""),
                report("r2", "user4", List.of("Off-topic"), "")));
        ReviewAdminDataAccessInterface reviewStore = new ReviewAdminDataAccessInterface() {
            public Optional<Review> getById(String id) { return Optional.of(review(id)); }
            public void deleteReview(String id) { }
        };
        Washroom w = TestSupport.washroom(); // "Test washroom", floor "2nd"
        WashroomDataAccessInterface washrooms = new WashroomDataAccessInterface() {
            public Optional<Washroom> getById(String id) { return Optional.of(w); }
            public List<Washroom> getNearby(double a, double b, double c) { return List.of(w); }
            public List<Washroom> getAll() { return List.of(w); }
        };
        final ModerateReviewsOutputData[] out = new ModerateReviewsOutputData[1];
        new ModerateReviewsInteractor(reportStore(reports), reviewStore, washrooms, moderators(true), d -> out[0] = d).loadReportedReviews();

        List<ReportedReview> queue = out[0].reportedReviews();
        TestSupport.check(queue.size() == 2, "both reported reviews shown at once");
        TestSupport.check(queue.get(0).reviewId().equals("r1"), "most-reported review sorted first");
        TestSupport.check(queue.get(0).totalReports() >= queue.get(1).totalReports(), "queue ordered by report count");
        TestSupport.check(queue.get(0).washroomName().equals("Test washroom — 2nd"),
                "washroom name resolved from the washroom store");
    }

    private static void moderatorQueueSkipsReportsForMissingReview() {
        final List<Report> reports = new ArrayList<>(List.of(report("gone", "user2", List.of("Spam"), "")));
        ReviewAdminDataAccessInterface reviewStore = new ReviewAdminDataAccessInterface() {
            public Optional<Review> getById(String id) { return Optional.empty(); } // review no longer exists
            public void deleteReview(String id) { }
        };
        final ModerateReviewsOutputData[] out = new ModerateReviewsOutputData[1];
        new ModerateReviewsInteractor(reportStore(reports), reviewStore, noWashrooms(), moderators(true), d -> out[0] = d).loadReportedReviews();
        TestSupport.check(out[0].reportedReviews().isEmpty(), "reports for a missing review are skipped");
    }

    private static void moderatorActionRequiresModeratorPrivilege() {
        final List<Report> reports = new ArrayList<>(List.of(report("r1", "user2", List.of("Spam"), "")));
        final Set<String> deletedReviews = new HashSet<>();
        final ModerateReviewsOutputData[] out = new ModerateReviewsOutputData[1];
        // moderators(false): the acting user is NOT a moderator.
        ModerateReviewsInputBoundary actor =
                new ModerateReviewsInteractor(reportStore(reports), reviewStore(deletedReviews), noWashrooms(), moderators(false), d -> out[0] = d);

        actor.removeReview(new ModerateReviewsInputData("r1", "not_a_mod"));
        TestSupport.check(!deletedReviews.contains("r1"), "review NOT removed by a non-moderator");
        TestSupport.check(!reports.isEmpty(), "reports NOT cleared by a non-moderator");
        TestSupport.check("Not authorized.".equals(out[0].message()), "unauthorized action is reported");
        TestSupport.check(out[0].reportedReviews().isEmpty(), "no queue is exposed to a non-moderator");

        actor.dismissReports(new ModerateReviewsInputData("r1", "not_a_mod"));
        TestSupport.check(!reports.isEmpty(), "dismiss also blocked for a non-moderator");
    }

    // --- fake helpers -----------------------------------------------------

    private static Report report(String reviewId, String user, List<String> reasons, String details) {
        return new Report("id-" + user + "-" + reviewId, reviewId, user, reasons, details, LocalDateTime.now());
    }

    private static Review review(String id) {
        return new Review(id, "w1", "author", 3, 3, "a comment", 0, LocalDate.now());
    }

    /** A report store backed by the given mutable list (getAll reads it; delete removes from it). */
    private static ReportedReviewsDataAccessInterface reportStore(List<Report> reports) {
        return new ReportedReviewsDataAccessInterface() {
            public List<Report> getAllReports() { return new ArrayList<>(reports); }
            public void deleteReportsForReview(String reviewId) {
                reports.removeIf(r -> r.reviewId().equals(reviewId));
            }
        };
    }

    /** A review admin store: every id resolves to a review until it is deleted. */
    private static ReviewAdminDataAccessInterface reviewStore(Set<String> deleted) {
        return new ReviewAdminDataAccessInterface() {
            public Optional<Review> getById(String id) {
                return deleted.contains(id) ? Optional.empty() : Optional.of(review(id));
            }
            public void deleteReview(String id) { deleted.add(id); }
        };
    }

    /** A moderator-authorization gateway that grants or denies everyone uniformly. */
    private static ModeratorDataAccessInterface moderators(boolean allowed) {
        return username -> allowed;
    }

    private static WashroomDataAccessInterface noWashrooms() {
        return new WashroomDataAccessInterface() {
            public Optional<Washroom> getById(String id) { return Optional.empty(); }
            public List<Washroom> getNearby(double a, double b, double c) { return List.of(); }
            public List<Washroom> getAll() { return List.of(); }
        };
    }
}
