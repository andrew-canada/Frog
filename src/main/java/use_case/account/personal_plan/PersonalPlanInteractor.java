package use_case.account.personal_plan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.User;
import entity.Washroom;
import use_case.port.CurrentUserSession;
import use_case.port.UserRepository;
import use_case.port.WashroomRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Generates, validates, and stores a user's washroom-break recommendation. */
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

    public PersonalPlanInteractor(UserRepository users, CurrentUserSession session, WashroomRepository washrooms,
                                  CalendarContentReader calendarReader, PersonalPlanGenerator generator,
                                  PersonalPlanOutputBoundary presenter) {
        this.users = users;
        this.session = session;
        this.washrooms = washrooms;
        this.calendarReader = calendarReader;
        this.generator = generator;
        this.presenter = presenter;
    }

    @Override
    public void execute(PersonalPlanInputData inputData) {
        User user = session.currentUser().orElse(null);
        if (user == null) {
            presenter.present(new PersonalPlanOutputData(false, "You need an account", ""));
            return;
        }
        if (inputData.calendarPath() == null || !inputData.calendarPath().endsWith(".ics")) {
            presenter.present(new PersonalPlanOutputData(false, "Please upload a .ics file", ""));
            return;
        }

        final int tripsPerDay;
        try {
            tripsPerDay = Integer.parseInt(inputData.nTrips());
            if (tripsPerDay < 1) throw new NumberFormatException();
        } catch (RuntimeException invalidTrips) {
            presenter.present(new PersonalPlanOutputData(false, "Please input a positive whole number of trips", ""));
            return;
        }

        try {
            List<Washroom> availableWashrooms = washrooms.getAll();
            String calendar = calendarReader.read(inputData.calendarPath());
            String generatedPlan = generator.generate(calendar, tripsPerDay, inputData.semester(), availableWashrooms);
            String personalPlan = normalizePlan(generatedPlan, availableWashrooms);
            User updatedUser = new User(user.username(), user.passwordHash(), personalPlan, user.moderator());
            users.save(updatedUser);
            session.setCurrentUser(updatedUser);
            presenter.present(new PersonalPlanOutputData(true, "Personal plan generated", personalPlan));
        } catch (Exception failure) {
            presenter.present(new PersonalPlanOutputData(false, "Could not generate a personal plan. Please try again.", ""));
        }
    }

    /** Converts the model response to the compact format consumed by the plan viewer and filter. */
    private static String normalizePlan(String generatedPlan, List<Washroom> availableWashrooms) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> suggestions = mapper.readValue(generatedPlan,
                new TypeReference<List<Map<String, String>>>() { });
        Map<String, Washroom> washroomsById = new LinkedHashMap<>();
        for (Washroom washroom : availableWashrooms) washroomsById.put(washroom.id(), washroom);

        List<Map<String, String>> plan = new ArrayList<>();
        for (Map<String, String> suggestion : suggestions) {
            String washroomId = suggestion.get(WASHROOM_ID);
            Washroom washroom = washroomsById.get(washroomId);
            if (washroom == null) throw new IllegalArgumentException("Recommendation contains an unknown washroom");
            String day = suggestion.get(DAY);
            String time = suggestion.get(TIME);
            if (day == null || day.isBlank() || time == null || time.isBlank()) {
                throw new IllegalArgumentException("Recommendation is missing a day or time");
            }
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("day", day);
            entry.put("time", time);
            entry.put("id", washroom.id());
            entry.put("name", washroom.name());
            plan.add(entry);
        }
        if (plan.isEmpty()) throw new IllegalArgumentException("Recommendation is empty");
        return mapper.writeValueAsString(plan);
    }
}
