package use_case.account.personal_plan;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.User;
import entity.Washroom;
import use_case.port.CurrentUserSession;
import use_case.port.UserRepository;
import use_case.port.WashroomRepository;

/**
 * Generates, validates, and stores a user's washroom-break recommendation.
 */
public final class PersonalPlanInteractor implements PersonalPlanInputBoundary {
    private static final String DAY = "Day of week";
    private static final String TIME = "Time (nearest hour) of washroom break";
    private static final String WASHROOM_ID = "Washroom id";

    private final UserRepository users;
    private final CurrentUserSession session;
    private final WashroomRepository washrooms;
    private final CalendarContentReader calendarReader;
    private final PersonalPlanGenerator generator;
    private final PersonalPlanOutputBoundary presenter;

    public PersonalPlanInteractor(final UserRepository users, final CurrentUserSession session,
                                  final WashroomRepository washrooms, final CalendarContentReader calendarReader,
                                  final PersonalPlanGenerator generator, final PersonalPlanOutputBoundary presenter) {
        this.users = users;
        this.session = session;
        this.washrooms = washrooms;
        this.calendarReader = calendarReader;
        this.generator = generator;
        this.presenter = presenter;
    }

    @Override
    public void execute(final PersonalPlanInputData inputData) {
        final User user = session
            .currentUser()
            .orElse(null);
        if (user == null) {
            presenter.present(new PersonalPlanOutputData(false, "You need an account", ""));
            return;
        }
        if (inputData.calendarPath() == null || !inputData
            .calendarPath()
            .endsWith(".ics")) {
            presenter.present(new PersonalPlanOutputData(false, "Please upload a .ics file", ""));
            return;
        }

        final int tripsPerDay;
        try {
            tripsPerDay = Integer.parseInt(inputData.nTrips());
            if (tripsPerDay < 1) {
                throw new NumberFormatException();
            }
        }
        catch (final RuntimeException invalidTrips) {
            presenter.present(new PersonalPlanOutputData(false, "Please input a positive whole number of trips", ""));
            return;
        }

        try {
            final List<Washroom> availableWashrooms = washrooms.getAll();
            final String calendar = calendarReader.read(inputData.calendarPath());
            final String generatedPlan =
                generator.generate(calendar, tripsPerDay, inputData.semester(), availableWashrooms);
            final String personalPlan = normalizePlan(generatedPlan, availableWashrooms);
            final User updatedUser = new User(user.username(), user.passwordHash(), personalPlan, user.moderator());
            users.save(updatedUser);
            session.setCurrentUser(updatedUser);
            presenter.present(new PersonalPlanOutputData(true, "Personal plan generated", personalPlan));
        }
        catch (final Exception failure) {
            presenter.present(
                new PersonalPlanOutputData(false, "Could not generate a personal plan. Please try again.", ""));
        }
    }

    /**
     * Converts the model response to the compact format consumed by the plan viewer and filter.
     * @param availableWashrooms parameter value.
     * @param generatedPlan parameter value.
     * @return the operation result.
     * @throws Exception if the plan cannot be parsed.
     */
    private static String normalizePlan(final String generatedPlan, final List<Washroom> availableWashrooms)
            throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final List<Map<String, String>> suggestions =
            mapper.readValue(generatedPlan, new TypeReference<List<Map<String, String>>>() {
            });
        final Map<String, Washroom> washroomsById = new LinkedHashMap<>();
        for (final Washroom washroom : availableWashrooms) {
            washroomsById.put(washroom.id(), washroom);
        }

        final List<Map<String, String>> plan = new ArrayList<>();
        for (final Map<String, String> suggestion : suggestions) {
            final String washroomId = suggestion.get(WASHROOM_ID);
            final Washroom washroom = washroomsById.get(washroomId);
            if (washroom == null) {
                throw new IllegalArgumentException("Recommendation contains an unknown washroom");
            }
            final String day = suggestion.get(DAY);
            final String time = suggestion.get(TIME);
            if (day == null || day.isBlank() || time == null || time.isBlank()) {
                throw new IllegalArgumentException("Recommendation is missing a day or time");
            }
            final Map<String, String> entry = new LinkedHashMap<>();
            entry.put("day", day);
            entry.put("time", time);
            entry.put("id", washroom.id());
            entry.put("name", washroom.name());
            plan.add(entry);
        }
        if (plan.isEmpty()) {
            throw new IllegalArgumentException("Recommendation is empty");
        }
        return mapper.writeValueAsString(plan);
    }
}
