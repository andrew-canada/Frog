import org.mindrot.jbcrypt.BCrypt;

import entity.Building;
import entity.MaintenanceIssue;
import entity.Review;
import entity.ReviewSummary;
import entity.StatusReport;
import entity.User;
import entity.Washroom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import use_case.filter.FilterInputData;
import use_case.filter.FilterInteractor;
import use_case.filter.FilterOutputBoundary;
import use_case.filter.FilterOutputData;
import use_case.filter.WashroomFilterCriteria;
import use_case.filter.WashroomFilterRepository;
import use_case.port.CurrentUserSession;
import use_case.port.ReviewRepository;
import use_case.port.StatusReportRepository;
import use_case.port.UserRepository;

final class FilterInteractorTest {
    static void run() {
        class FakeWashrooms implements WashroomFilterRepository {
            private final List<Washroom> washrooms = new ArrayList<>();

            FakeWashrooms() {
                washrooms.add(new Washroom(
                    "a",
                    "a",
                    new Building("BA", "a", 44.0, 72.0),
                    "1",
                    true,
                    Washroom.Gender.ALL_GENDER,
                    0,
                    0,
                    "none",
                    new ReviewSummary(1.0, 1.0, 1)));
                washrooms.add(new Washroom(
                    "b",
                    "b",
                    new Building("a", "a", 44.0, 72.0),
                    "1",
                    false,
                    Washroom.Gender.ALL_GENDER,
                    0,
                    0,
                    "none",
                    new ReviewSummary(1.0, 1.0, 1)));
                washrooms.add(new Washroom(
                    "c",
                    "c",
                    new Building("b", "b", 45.0, 72.0),
                    "1",
                    true,
                    Washroom.Gender.ALL_GENDER,
                    0,
                    0,
                    "none",
                    new ReviewSummary(1.0, 1.0, 1)));
            }

            @Override
            public Optional<Washroom> getById(final String Id) {
                return Optional.of(washrooms.get(0));
            }

            @Override
            public List<Washroom> getNearby(final double latitude, final double longitude, final double radiusMeters) {
                return new ArrayList<>(List.of(new Washroom[] {washrooms.get(1), washrooms.get(0)}));
            }

            @Override
            public List<Washroom> getAll() {
                return washrooms;
            }

            @Override
            public List<Washroom> findMatching(final WashroomFilterCriteria criteria) {
                if (criteria.accessibleOnly()) {
                    return new ArrayList<>(Collections.singleton(washrooms.get(0)));
                }
                if (criteria.gender() != null && criteria.gender().contains(Washroom.Gender.WOMEN)) {
                    return new ArrayList<>(Collections.singleton(washrooms.get(1)));
                }
                if (criteria.buildingCode() != null && criteria.buildingCode().equals("BA")) {
                    return new ArrayList<>(List.of(new Washroom[]{washrooms.get(0), washrooms.get(2)}));
                }
                return new ArrayList<>(Collections.singleton(washrooms.get(2)));
            }
        }

        class FakeReviews implements ReviewRepository {
            private final List<Review> reviews = new ArrayList<>();

            FakeReviews() {
                reviews.add(new Review(
                    "a", "a", "", 4.0, 4.0,
                    "", 5, null));
                reviews.add(new Review(
                    "b", "a", "", 2.0, 2.0,
                    "", 1, null));
                reviews.add(new Review(
                    "c", "c", "", 5.0, 4.0,
                    "", 5, null));
            }

            @Override
            public List<Review> getReviewsForWashroom(final String washroomId) {
                return new ArrayList<>(Collections.singleton(reviews.getFirst()));
            }

            @Override
            public ReviewSummary getSummary(final String washroomId) {
                return new ReviewSummary(1.0, 1.0, 1);
            }

            @Override
            public List<Review> getReviewsByUser(final String username) {
                final List<Review> byUser = new ArrayList<>();
                byUser.add(reviews.get(0));
                byUser.add(reviews.get(2));

                return byUser;
            }

            @Override
            public void save(final Review review) {
                reviews.add(review);
            }
        }

        class FakeUser implements UserRepository, CurrentUserSession {
            User current;
            final User saved = new User("demo", BCrypt.hashpw("secret", BCrypt.gensalt()), "");

            @Override
            public Optional<User> get(final String n) {
                return Optional.of(saved);
            }

            @Override
            public boolean existsByName(final String n) {
                return true;
            }

            @Override
            public void save(final User u) {
            }

            @Override
            public Optional<User> currentUser() {
                return Optional.ofNullable(current);
            }

            @Override
            public void setCurrentUser(final User u) {
                current = u;
            }

            @Override
            public void clear() {
                current = null;
            }

            @Override
            public void removeUser(final String n) {
            }
        }

        class FakeStatus implements StatusReportRepository {

            @Override
            public void save(final StatusReport report) {
            }

            @Override
            public List<StatusReport> getRecentForWashroom(final String washroomId, final LocalDateTime since) {
                return new ArrayList<>();
            }

            @Override
            public List<StatusReport> getForWashroom(final String washroomId, final LocalDateTime from, final LocalDateTime to) {
                return new ArrayList<>();
            }

            @Override
            public Map<String, StatusReport> getCurrentHourForWashrooms(final List<String> washroomIds, final int hour) {
                final Map<String, StatusReport> map = new HashMap<>();
                for (final String washroomId : washroomIds) {
                    map.put(washroomId,
                        new StatusReport(washroomId, "", 3, 3, MaintenanceIssue.NONE, LocalDateTime.now()));
                }
                return map;
            }
        }

        class FakePresenter implements FilterOutputBoundary {
            FilterOutputData out;

            @Override
            public void present(final FilterOutputData data) {
                out = data;
            }

            @Override
            public void presentError(final String message) {
                System.out.println(message);
            }
        }

        final FakeWashrooms washrooms = new FakeWashrooms();
        final FakeReviews reviews = new FakeReviews();
        final FakeUser users = new FakeUser();
        final FakeStatus status = new FakeStatus();
        final FakePresenter output = new FakePresenter();

        new FilterInteractor(
            washrooms,
            reviews,
            status,
            users,
            output,
            new HashSet<String>()).execute(
            new FilterInputData(5.0F, 1.0F, false, null, "", false, false, 1.0, 1.0)
        );

        TestSupport.check(output.out.success()
            && output.out
            .washrooms()
            .size() == 1, "valid filter should only return one washroom here");
        TestSupport.check(output.out.success()
                && output.out
                .washrooms()
                .getFirst().id().equals("c"), "valid filter should return washroom C here");

        new FilterInteractor(
                washrooms,
                reviews,
                status,
                users,
                output,
                new HashSet<String>()).execute(
                new FilterInputData(5.0F, 1.0F, true, null, "", false, false, 1.0, 1.0)
        );

        TestSupport.check(output.out.success()
                && output.out
                .washrooms()
                .getFirst().id().equals("a"), "valid filter should return washroom A here");

        new FilterInteractor(
                washrooms,
                reviews,
                status,
                users,
                output,
                new HashSet<String>()).execute(
                new FilterInputData(5.0F, 1.0F, false, "WOMEN", "", false, false, 1.0, 1.0)
        );

        TestSupport.check(output.out.success()
                && output.out
                .washrooms()
                .getFirst().id().equals("b"), "valid filter should return washroom B here");

        new FilterInteractor(
                washrooms,
                reviews,
                status,
                users,
                output,
                new HashSet<String>()).execute(
                new FilterInputData(5.0F, 1.0F, false, null, "a", false, false, 1.0, 1.0)
        );

        TestSupport.check(output.out.success()
                && output.out
                .washrooms()
                .stream()
                .map(Washroom::id).toList()
                .equals(List.of(new String[]{"a", "c"})), "valid filter should return washrooms A and C here");
    }
}

