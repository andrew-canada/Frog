import data_access.AbstractCondition;
import data_access.status.StatusReportDataAccessInterface;
import data_access.user.UserDataAccessInterface;
import data_access.washroom.WashroomDataAccessInterface;
import entity.*;
import data_access.review.ReviewDataAccessInterface;
import org.mindrot.jbcrypt.BCrypt;
import use_case.filter.FilterInputData;
import use_case.filter.FilterInteractor;
import use_case.filter.FilterOutputBoundary;
import use_case.filter.FilterOutputData;

import java.time.LocalDateTime;
import java.util.*;

final class FilterInteractorTest {
    static void run() {
        class FakeWashrooms implements WashroomDataAccessInterface {
            private List<Washroom> washrooms = new ArrayList<>();

            public FakeWashrooms() {
                washrooms.add(new Washroom(
                        "a",
                        "a",
                        new Building("a","a",44.0,72.0),
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
                        new Building("a","a",44.0,72.0),
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
                        new Building("b","b",45.0,72.0),
                        "1",
                        true,
                        Washroom.Gender.ALL_GENDER,
                        0,
                        0,
                        "none",
                        new ReviewSummary(1.0, 1.0, 1)));
            }
            @Override
            public Optional<Washroom> getById(String Id) {
                return Optional.of(washrooms.get(0));
            }

            @Override
            public List<Washroom> getNearby(double latitude, double longitude, double radiusMeters) {
                return new ArrayList<>(List.of(new Washroom[]{washrooms.get(1), washrooms.get(0)}));
            }

            @Override
            public List<Washroom> getAll() {
                return washrooms;
            }

            @Override
            public List<Washroom> getMatching(Iterable<AbstractCondition<?>> conditions) {
                return new ArrayList<>(Collections.singleton(washrooms.get(2)));
            }

            @Override
            public Map<String, Washroom> getMatchingIDMap(Iterable<AbstractCondition<?>> conditions) {
                Washroom washroom = washrooms.get(2);
                Map<String, Washroom> map = new HashMap<>();
                map.put(washroom.id(), washroom);
                return map;
            }
        }

        class FakeReviews implements ReviewDataAccessInterface {
            private List<Review> reviews = new ArrayList<>();

            public FakeReviews() {
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
            public List<Review> getReviewsForWashroom(String washroomId) {
                return new ArrayList<>(Collections.singleton(reviews.getFirst()));
            }

            @Override
            public ReviewSummary getSummary(String washroomId) {
                return new ReviewSummary(1.0, 1.0, 1);
            }

            @Override
            public List<Review> getReviewsByUser(String username) {
                List<Review> byUser = new ArrayList<>();
                byUser.add(reviews.get(0));
                byUser.add(reviews.get(2));

                return byUser;
            }
        }

        class FakeUser implements UserDataAccessInterface {
            User current;
            User saved = new User("demo", BCrypt.hashpw("secret", BCrypt.gensalt()), "");

            @Override
            public Optional<User> get(String n) {
                return Optional.of(saved);
            }

            @Override
            public boolean existsByName(String n) {
                return true;
            }

            @Override
            public void save(User u) {
            }

            @Override
            public void setCurrentUser(User u) {
                current = u;
            }

            @Override
            public Optional<User> getCurrentUser() {
                return Optional.ofNullable(current);
            }

            @Override
            public void removeUser(String n) {
            }
        }

        class FakeStatus implements StatusReportDataAccessInterface {

            @Override
            public void save(StatusReport report) {}

            @Override
            public List<StatusReport> getRecentForWashroom(String washroomId, LocalDateTime since) {
                return new ArrayList<>();
            }

            @Override
            public List<StatusReport> getForWashroom(String washroomId, LocalDateTime from, LocalDateTime to) {
                return new ArrayList<>();
            }

            @Override
            public Map<String, StatusReport> getCurrentHourForWashrooms(List<String> washroomIds, int hour) {
                Map<String, StatusReport> map = new HashMap<>();
                for (String washroomId : washroomIds) {
                    map.put(washroomId, new StatusReport(washroomId, "", 3, 3, MaintenanceIssue.NONE, LocalDateTime.now()));
                }
                return map;
            }
        }

        class FakePresenter implements FilterOutputBoundary {
            public FilterOutputData out;
            @Override
            public void present(FilterOutputData data) {
                out = data;
            }

            @Override
            public void presentError(String message) {
                System.out.println(message);
            }
        }

        FakeWashrooms washrooms = new FakeWashrooms();
        FakeReviews reviews = new FakeReviews();
        FakeUser users = new FakeUser();
        FakeStatus status = new FakeStatus();
        FakePresenter output = new FakePresenter();

        new FilterInteractor(
                washrooms,
                reviews,
                status,
                users,
                output,
        new HashSet<String>()).execute(
                new FilterInputData(5.0F, 1.0F, false, null, "", false, false,1.0, 1.0)
        );

        TestSupport.check(output.out.success()
                && output.out.washrooms().size() == 1, "valid filter should only return one washroom here");
    }
}

