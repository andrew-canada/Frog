import entity.Report;
import entity.Review;
import entity.ReviewSummary;
import entity.Washroom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import use_case.port.ReviewRepository;
import use_case.port.WashroomRepository;
import use_case.view_reviews.ViewReviewsInputData;
import use_case.view_reviews.ViewReviewsInteractor;
import use_case.view_reviews.ViewReviewsOutputBoundary;
import use_case.view_reviews.ViewReviewsOutputData;

final class ViewReviewsInteractorTest {
    static void run() {
        final Washroom w = TestSupport.washroom();
        final ReviewRepository reviews = new ReviewRepository() {
            @Override
            public List<Review> getReviewsForWashroom(final String id) {
                return List.of(new Review("1", id, "a", 4, 4, "less helpful", 2, LocalDate.now()),
                    new Review("2", id, "b", 5, 5, "most helpful", 9, LocalDate.now()));
            }

            @Override
            public ReviewSummary getSummary(final String id) {
                return new ReviewSummary(4.5, 4.5, 2);
            }

            @Override
            public List<Review> getReviewsByUser(final String u) {
                return List.of();
            }

            @Override
            public void save(final Review review) {
            }
        };
        final WashroomRepository washrooms = new WashroomRepository() {
            @Override
            public Optional<Washroom> getById(final String id) {
                return Optional.of(w);
            }

            @Override
            public List<Washroom> getNearby(final double a, final double b, final double c) {
                return List.of(w);
            }

            @Override
            public List<Washroom> getAll() {
                return List.of(w);
            }
        };
        final ViewReviewsOutputData[] result = new ViewReviewsOutputData[1];
        final ViewReviewsOutputBoundary output = new ViewReviewsOutputBoundary() {
            @Override
            public void present(final ViewReviewsOutputData d) {
                result[0] = d;
            }

            @Override
            public void presentError(final String m) {
                throw new AssertionError(m);
            }
        };
        final use_case.vote_helpful.HelpfulVoteDataAccessInterface votes =
            new use_case.vote_helpful.HelpfulVoteDataAccessInterface() {
                @Override
                public boolean hasVoted(final String r, final String u) {
                    return false;
                }

                @Override
                public void addVote(final String r, final String u) {
                }

                @Override
                public void removeVote(final String r, final String u) {
                }
            };
        final use_case.report_review.ReviewReportDataAccessInterface reports =
            new use_case.report_review.ReviewReportDataAccessInterface() {
                @Override
                @Override
                public void save(final Report report) {
                }

                @Override
                @Override
                public boolean hasReported(final String r, final String u) {
                    return false;
                }

            };
        new ViewReviewsInteractor(reviews, washrooms, votes, reports, output).execute(
            new ViewReviewsInputData("w1", "tester"));
        TestSupport.check(result[0]
            .washroomName()
            .equals("Bahen"), "building name");
        TestSupport.check(result[0]
            .subtitle()
            .equals("Test"), "cleaned description");
        TestSupport.check(result[0].reviewCount() == 2, "summary count");
        TestSupport.check(result[0]
            .reviews()
            .get(0)
            .helpfulCount() == 9, "most helpful first");
    }
}
