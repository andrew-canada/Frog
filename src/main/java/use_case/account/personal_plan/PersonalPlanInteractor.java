package use_case.account.personal_plan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import entity.User;
import data_access.user.UserDataAccessInterface;
import com.google.genai.Client;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PersonalPlanInteractor implements PersonalPlanInputBoundary {

    public static final String GEMINI_API_KEY_ENV = "GEMINI_API_KEY";

    private final String dayPrompt = "Day of week";
    private final String timePrompt = "Time (nearest hour) of washroom break";
    private final String washroomPrompt = "Washroom";

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
                String personalPlan = response.text();
                if (checkValid(response.text(), Integer.parseInt(inputData.nTrips()))) {
                    throw new Exception();
                }
                System.out.println(personalPlan);
                users.removeUser(user.username());
                System.out.println(1);
                User newUser = new User(user.username(), user.passwordHash(), personalPlan);
                System.out.println(2);
                users.save(newUser);
                System.out.println(3);
                users.setCurrentUser(newUser);
                System.out.println(4);
                presenter.present(new PersonalPlanOutputData(true, "", personalPlan));
                System.out.println(5);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                presenter.present(new PersonalPlanOutputData(false, "Try again", ""));
            }
        }

    }

    public class WashroomPlan {
        @JsonProperty(dayPrompt)
        public String day;
        @JsonProperty(timePrompt)
        public String time;
        @JsonProperty(washroomPrompt)
        public String washroom;
    }

    public class EntirePlan {
        public List<WashroomPlan> washrooms;
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

}
