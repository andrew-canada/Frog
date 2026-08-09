import entity.Building;
import entity.Report;
import entity.Review;
import entity.ReviewSummary;
import entity.User;
import entity.Washroom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import use_case.port.CurrentUserSession;
import use_case.port.ReviewRepository;
import use_case.port.WashroomRepository;
import use_case.report_review.ReviewReportDataAccessInterface;
import use_case.sort_review.ReviewSortOrder;
import use_case.sort_review.SortReviewInputData;
import use_case.sort_review.SortReviewInteractor;
import use_case.sort_washrooms.SortWashroomInputData;
import use_case.sort_washrooms.SortWashroomInteractor;
import use_case.sort_washrooms.SortWashroomsOutputBoundary;
import use_case.sort_washrooms.SortWashroomsOutputData;
import use_case.sort_washrooms.WashroomSortOrder;
import use_case.view_reviews.ViewReviewsOutputBoundary;
import use_case.view_reviews.ViewReviewsOutputData;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;

final class SortingInteractorTest {
    static void run() {
        washroomSortUsesBulkLookup();
        alphabeticalSortUsesDisplayedBuildingName();
        reviewSortUsesOneReviewReadAndBatchUserState();
    }

    private static void washroomSortUsesBulkLookup() {
        final Washroom first = washroom("one", "Zulu", 3);
        final Washroom second = washroom("two", "Alpha", 5);
        class BulkWashrooms implements WashroomRepository {
            int bulkLookups;

            @Override
            public Optional<Washroom> getById(final String id) {
                throw new AssertionError("sorting should use the bulk lookup");
            }

            @Override
            public List<Washroom> getByIds(final java.util.Collection<String> ids) {
                bulkLookups++;
                return List.of(first, second);
            }

            @Override
            public List<Washroom> getNearby(final double latitude, final double longitude, final double radiusMeters) {
                return List.of();
            }

            @Override
            public List<Washroom> getAll() {
                return List.of(first, second);
            }
        }
        final BulkWashrooms washrooms = new BulkWashrooms();
        final SortWashroomsOutputData[] result = new SortWashroomsOutputData[1];
        final SortWashroomsOutputBoundary output = data -> result[0] = data;

        new SortWashroomInteractor(washrooms, output).execute(new SortWashroomInputData(
            WashroomSortOrder.HIGHEST_RATED, List.of("one", "two"), 0, 0));

        TestSupport.check(washrooms.bulkLookups == 1, "washroom sorting must use one bulk lookup");
        TestSupport.check(result[0]
            .washrooms()
            .getFirst()
            .id()
            .equals("two"), "washrooms should still sort by rating");
    }

    private static void alphabeticalSortUsesDisplayedBuildingName() {
        final Washroom displayedAsZulu = washroom("zulu", "Alpha raw name", "Zulu Building", 4);
        final Washroom displayedAsAlpha = washroom("alpha", "Zulu raw name", "Alpha Building", 4);
        final WashroomRepository washrooms = new SingleWashroomRepository(List.of(displayedAsZulu, displayedAsAlpha));
        final SortWashroomsOutputData[] result = new SortWashroomsOutputData[1];

        new SortWashroomInteractor(washrooms, data -> result[0] = data).execute(new SortWashroomInputData(
            WashroomSortOrder.ALPHABETICAL, List.of("zulu", "alpha"), 0, 0));

        TestSupport.check(result[0]
                .washrooms()
                .getFirst()
                .id()
                .equals("alpha"),
            "alphabetical sorting should use the building name shown on each card");
    }

    private static void reviewSortUsesOneReviewReadAndBatchUserState() {
        final Washroom washroom = washroom("washroom", "Test", 4);
        class CountingReviews implements ReviewRepository {
            int reviewReads;

            @Override
            public List<Review> getReviewsForWashroom(final String washroomId) {
                reviewReads++;
                return List.of(new Review("low", washroomId, "author", 2, 2, "low", 0, LocalDate.now()),
                    new Review("high", washroomId, "author", 5, 5, "high", 1, LocalDate.now()));
            }

            @Override
            public ReviewSummary getSummary(final String washroomId) {
                throw new AssertionError("sorting should summarize the reviews already loaded");
            }

            @Override
            public List<Review> getReviewsByUser(final String username) {
                return List.of();
            }

            @Override
            public void save(final Review review) {
            }
        }
        final CountingReviews reviews = new CountingReviews();
        final WashroomRepository washrooms = new SingleWashroomRepository(washroom);
        class BatchVotes implements HelpfulVoteDataAccessInterface {
            int batches;

            @Override
            public boolean hasVoted(final String reviewId, final String username) {
                throw new AssertionError("sorting should batch vote checks");
            }

            @Override
            public Set<String> votedReviewIds(final java.util.Collection<String> reviewIds, final String username) {
                batches++;
                return Set.of("high");
            }

            @Override
            public void addVote(final String reviewId, final String username) {
            }

            @Override
            public void removeVote(final String reviewId, final String username) {
            }
        }
        class BatchReports implements ReviewReportDataAccessInterface {
            int batches;

            @Override
            public void save(final Report report) {
            }

            @Override
            public boolean hasReported(final String reviewId, final String username) {
                throw new AssertionError("sorting should batch report checks");
            }

            @Override
            public Set<String> reportedReviewIds(final java.util.Collection<String> reviewIds, final String username) {
                batches++;
                return Set.of();
            }
        }
        final BatchVotes votes = new BatchVotes();
        final BatchReports reports = new BatchReports();
        final CurrentUserSession noUser = new CurrentUserSession() {
            @Override
            public Optional<User> currentUser() {
                return Optional.empty();
            }

            @Override
            public void setCurrentUser(final User user) {
            }

            @Override
            public void clear() {
            }
        };
        final ViewReviewsOutputData[] result = new ViewReviewsOutputData[1];
        final ViewReviewsOutputBoundary output = new ViewReviewsOutputBoundary() {
            @Override
            public void present(final ViewReviewsOutputData data) {
                result[0] = data;
            }

            @Override
            public void presentError(final String message) {
                throw new AssertionError(message);
            }
        };

        new SortReviewInteractor(reviews, noUser, washrooms, votes, reports, output)
            .execute(new SortReviewInputData(ReviewSortOrder.HIGHEST_RATED, washroom.id()));

        TestSupport.check(reviews.reviewReads == 1, "review sorting must read reviews once");
        TestSupport.check(votes.batches == 1 && reports.batches == 1, "review state should be batched");
        TestSupport.check(result[0]
            .reviews()
            .getFirst()
            .reviewId()
            .equals("high"), "reviews should still sort by rating");
        TestSupport.check(result[0]
            .reviews()
            .getFirst()
            .votedByCurrentUser(), "batched vote state should be displayed");
    }

    private static Washroom washroom(final String id, final String name, final double rating) {
        return washroom(id, name, "Bahen", rating);
    }

    private static Washroom washroom(final String id, final String name, final String buildingName, final double rating) {
        return new Washroom(id, name, new Building("BA", buildingName, 43.66, -79.39), "2", true,
            Washroom.Gender.ALL_GENDER, 1, 1, "Test", new ReviewSummary(rating, rating, 1));
    }

    private record SingleWashroomRepository(List<Washroom> washrooms) implements WashroomRepository {
        SingleWashroomRepository(final Washroom washroom) {
            this(List.of(washroom));
        }

        @Override
        public Optional<Washroom> getById(final String id) {
            return washrooms
                .stream()
                .filter(washroom -> washroom
                    .id()
                    .equals(id))
                .findFirst();
        }

        @Override
        public List<Washroom> getNearby(final double latitude, final double longitude, final double radiusMeters) {
            return washrooms;
        }

        @Override
        public List<Washroom> getAll() {
            return washrooms;
        }
    }
}
