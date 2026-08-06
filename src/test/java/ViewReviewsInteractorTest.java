import data_access.washroom.WashroomDataAccessInterface;
import entity.*;
import use_case.view_reviews.*;

import java.time.LocalDate;
import java.util.*;

final class ViewReviewsInteractorTest {
    static void run() {
        Washroom w = TestSupport.washroom();
        ReviewDataAccessInterface reviews = new ReviewDataAccessInterface() {
            public List<Review> getReviewsForWashroom(String id) {
                return List.of(new Review("1", id, "a", 4, 4, "less helpful", 2, LocalDate.now()), new Review("2", id, "b", 5, 5, "most helpful", 9, LocalDate.now()));
            }

            public ReviewSummary getSummary(String id) {
                return new ReviewSummary(4.5, 4.5, 2);
            }

            public List<Review> getReviewsByUser(String u) {
                return List.of();
            }
        };
        WashroomDataAccessInterface washrooms = new WashroomDataAccessInterface() {
            public Optional<Washroom> getById(String id) {
                return Optional.of(w);
            }

            public List<Washroom> getNearby(double a, double b, double c) {
                return List.of(w);
            }

            public List<Washroom> getAll() {
                return List.of(w);
            }
        };
        final ViewReviewsOutputData[] result = new ViewReviewsOutputData[1];
        ViewReviewsOutputBoundary output = new ViewReviewsOutputBoundary() {
            public void present(ViewReviewsOutputData d) {
                result[0] = d;
            }

            public void presentError(String m) {
                throw new AssertionError(m);
            }
        };
        use_case.vote_helpful.HelpfulVoteDataAccessInterface votes = new use_case.vote_helpful.HelpfulVoteDataAccessInterface() {
            public boolean hasVoted(String r, String u) { return false; }
            public void addVote(String r, String u) { }
            public void removeVote(String r, String u) { }
        };
        use_case.report_review.ReviewReportDataAccessInterface reports = new use_case.report_review.ReviewReportDataAccessInterface() {
            public void save(Report report) { }
            public boolean hasReported(String r, String u) { return false; }

        };
        new ViewReviewsInteractor(reviews, washrooms, votes, reports, output).execute(new ViewReviewsInputData("w1", "tester"));
        TestSupport.check(result[0].washroomName().equals("Bahen"), "building name");
        TestSupport.check(result[0].subtitle().equals("Test"), "cleaned description");
        TestSupport.check(result[0].reviewCount() == 2, "summary count");
        TestSupport.check(result[0].reviews().get(0).helpfulCount() == 9, "most helpful first");
    }
}
