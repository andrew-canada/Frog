import entity.Building;
import entity.ReviewSummary;
import entity.User;
import entity.Washroom;
import java.util.List;
import java.util.Optional;
import use_case.account.personal_plan.CalendarContentReader;
import use_case.account.personal_plan.PersonalPlanGenerator;
import use_case.account.personal_plan.PersonalPlanInputData;
import use_case.account.personal_plan.PersonalPlanInteractor;
import use_case.account.personal_plan.PersonalPlanOutputBoundary;
import use_case.account.personal_plan.PersonalPlanOutputData;
import use_case.port.CurrentUserSession;
import use_case.port.UserRepository;
import use_case.port.WashroomRepository;

final class PersonalPlanInteractorTest {
    static void run() {
        final Washroom washroom = new Washroom("w1", "Accessible washroom",
            new Building("BA", "Bahen", 43.66, -79.39), "2", true,
            Washroom.Gender.ALL_GENDER, 1, 1, "Test", ReviewSummary.empty());
        class Users implements UserRepository, CurrentUserSession {
            User current = new User("student", "hash", "", true);
            User saved;

            @Override
            public Optional<User> get(final String username) {
                return Optional.of(current);
            }

            @Override
            public boolean existsByName(final String username) {
                return true;
            }

            @Override
            public void save(final User user) {
                saved = user;
            }

            @Override
            public void removeUser(final String username) {
            }

            @Override
            public Optional<User> currentUser() {
                return Optional.ofNullable(current);
            }

            @Override
            public void setCurrentUser(final User user) {
                current = user;
            }

            @Override
            public void clear() {
                current = null;
            }
        }
        class Generator implements PersonalPlanGenerator {
            String semester;

            @Override
            public String generate(final String calendar, final int tripsPerDay, final String selectedSemester,
                                   final List<Washroom> availableWashrooms) {
                semester = selectedSemester;
                TestSupport.check(tripsPerDay == 2 && availableWashrooms.equals(List.of(washroom)),
                    "generator should receive the selected recommendations context");
                return "[{\"Day of week\":\"Mon\",\"Time (nearest hour) of washroom break\":\"10:00\",\"Washroom " 
                    + "id\":\"w1\"}]";
            }
        }
        final Users users = new Users();
        final Generator generator = new Generator();
        final WashroomRepository washrooms = new WashroomRepository() {
            @Override
            public Optional<Washroom> getById(final String id) {
                return Optional.of(washroom);
            }

            @Override
            public List<Washroom> getNearby(final double latitude, final double longitude, final double radiusMeters) {
                return List.of(washroom);
            }

            @Override
            public List<Washroom> getAll() {
                return List.of(washroom);
            }
        };
        final PersonalPlanOutputData[] result = new PersonalPlanOutputData[1];
        final PersonalPlanOutputBoundary presenter = output -> {
            result[0] = output;
        };
        final CalendarContentReader calendar = path -> {
            return "calendar";
        };

        new PersonalPlanInteractor(users, users, washrooms, calendar, generator, presenter)
            .execute(new PersonalPlanInputData("schedule.ics", "2", "Winter"));

        TestSupport.check(result[0].success(), "a valid recommendation should be saved");
        TestSupport.check(generator.semester.equals("Winter"), "selected semester should reach the generator");
        TestSupport.check(users.saved
                .personalPlan()
                .contains("\"id\":\"w1\"")
                && users.saved
                .personalPlan()
                .contains("\"name\":\"Accessible washroom\""),
            "saved recommendations should support the plan view and personal-plan filter");
        TestSupport.check(users.current.moderator(), "saving a plan must retain the user's moderator role");
    }
}
