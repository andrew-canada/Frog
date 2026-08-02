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
        new ViewReviewsInteractor(reviews, washrooms, output).execute(new ViewReviewsInputData("w1"));
        TestSupport.check(result[0].reviewCount() == 2, "summary count");
        TestSupport.check(result[0].reviews().get(0).helpfulCount() == 9, "most helpful first");
    }
}
