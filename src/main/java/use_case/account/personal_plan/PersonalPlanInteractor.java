package use_case.account.personal_plan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import data_access.user.UserDataAccessInterface;
import entity.User;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PersonalPlanInteractor implements PersonalPlanInputBoundary {

    public static final String GEMINI_API_KEY_ENV = "GEMINI_API_KEY";

    private static final String dayPrompt = "Day of week";
    private static final String timePrompt = "Time (nearest hour) of washroom break";
    private static final String washroomPrompt = "Washroom";

    private final UserDataAccessInterface users;
    private final PersonalPlanOutputBoundary presenter;

    public PersonalPlanInteractor(UserDataAccessInterface users, PersonalPlanOutputBoundary presenter) {
        this.users = users;
        this.presenter = presenter;
    }

    @Override
    public void execute(PersonalPlanInputData inputData) {

        User user = users.getCurrentUser().orElse(null);
        String calendar = getFile(inputData.calendarPath());

        if (user == null) {
            presenter.present(new PersonalPlanOutputData(false, "You need an account", ""));
        } else if (calendar == null) {
            presenter.present(new PersonalPlanOutputData(false, "Please upload a .ics file", ""));
        } else if (!isInt(inputData.nTrips())) {
            presenter.present(new PersonalPlanOutputData(false, "Please input an integer", ""));
        } else {
            String apiKey = System.getenv(GEMINI_API_KEY_ENV);
            if (apiKey == null || apiKey.isBlank()) {
                presenter.present(new PersonalPlanOutputData(false, "Set " + GEMINI_API_KEY_ENV + " to generate a personal plan", ""));
                return;
            }
            try {

                Schema washroom = Schema.builder().type(Type.Known.OBJECT).properties(Map.of(
                        dayPrompt, Schema.builder().type(Type.Known.STRING).build(),
                        timePrompt, Schema.builder().type(Type.Known.STRING).build(),
                        washroomPrompt, Schema.builder().type(Type.Known.STRING).build()
                )).required(List.of(dayPrompt, timePrompt, washroomPrompt)).build();

                Schema plan = Schema.builder().type(Type.Known.ARRAY).items(washroom).build();

                GenerateContentConfig config = GenerateContentConfig.builder().responseMimeType("application/json").responseSchema(plan).build();

                Client client = Client.builder().apiKey(apiKey).build();
                GenerateContentResponse response = client.models.generateContent("gemini-3.6-flash", "This is a UOFT time table, generate a schedule of washroom breaks in the fall semester such that there are " + inputData.nTrips() + "washroom trips per day they are at school: " + calendar, config);
                String personalPlanString = response.text();
                if (checkValid(response.text(), Integer.parseInt(inputData.nTrips()))) {
                    throw new Exception();
                }
                System.out.println(personalPlanString);
                users.removeUser(user.username());
                User newUser = new User(user.username(), user.passwordHash(), personalPlanString);
                users.save(newUser);
                users.setCurrentUser(newUser);
                presenter.present(new PersonalPlanOutputData(true, "", personalPlanString));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                presenter.present(new PersonalPlanOutputData(false, "Try again", ""));
            }
        }

    }

    private boolean checkValid(String response, int n) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            EntirePlan entirePlan = mapper.readValue(response, EntirePlan.class);
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            for (WashroomPlan washroomPlan : entirePlan.washrooms) {
                if (map.containsKey(washroomPlan.day)) {
                    map.put(washroomPlan.day, 1);
                } else {
                    map.put(washroomPlan.day, map.get(washroomPlan.day) + 1);
                }
            }
            List<String> days = new ArrayList<String>(map.keySet());
            assert days.size() <= 7;
            for (String day : days) {
                assert map.get(day) == n;
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private HashMap<String, List<List<String>>> extractPlan(String response) {

        try {
            HashMap<String, List<List<String>>> plan = new HashMap<>();
            ObjectMapper mapper = new ObjectMapper();
            EntirePlan entirePlan = mapper.readValue(response, EntirePlan.class);
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            for (WashroomPlan washroomPlan : entirePlan.washrooms) {
                map.put(washroomPlan.day, 1);
            }
            List<String> days = new ArrayList<String>(map.keySet());
            for (String day : days) {
                List<List<String>> dayPlan = new ArrayList<>();
                for (WashroomPlan washroomPlan : entirePlan.washrooms) {
                    if (washroomPlan.day.equals(day)) {
                        dayPlan.add(List.of(washroomPlan.time, washroomPlan.washroom));
                    }
                }
                plan.put(day, dayPlan);
            }
            return plan;
        } catch (Exception e) {
            return null;
        }

    }

    private boolean isInt(String str) {

        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private String getFile(String str) {

        try {
            return Files.readString(Paths.get(str));
        } catch (Exception e) {
            return null;
        }

    }

    public static class WashroomPlan {
        @JsonProperty(dayPrompt)
        public String day;
        @JsonProperty(timePrompt)
        public String time;
        @JsonProperty(washroomPrompt)
        public String washroom;
    }

    public static class EntirePlan {
        public List<WashroomPlan> washrooms;
    }

}
