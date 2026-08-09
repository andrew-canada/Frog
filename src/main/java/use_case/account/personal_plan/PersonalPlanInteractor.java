package use_case.account.personal_plan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import data_access.user.UserDataAccessInterface;
import data_access.washroom.WashroomDataAccessInterface;
import entity.User;
import entity.Washroom;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public final class PersonalPlanInteractor implements PersonalPlanInputBoundary {

    public static final String GEMINI_API_KEY_ENV = "GEMINI_API_KEY";

    private static final String DAY_PROMPT = "Day of week";
    private static final String TIME_PROMPT = "Time (nearest hour) of washroom break";
    private static final String WASHROOM_ID_PROMPT = "Washroom id";

    private final UserDataAccessInterface users;
    private final WashroomDataAccessInterface washrooms;
    private final PersonalPlanOutputBoundary presenter;

    public PersonalPlanInteractor(UserDataAccessInterface users, WashroomDataAccessInterface washrooms, PersonalPlanOutputBoundary presenter) {
        this.users = users;
        this.washrooms = washrooms;
        this.presenter = presenter;
    }

    @Override
    public void execute(PersonalPlanInputData inputData) {

        User user = users.getCurrentUser().orElse(null);
        String path = inputData.calendarPath();
        String calendar = getFile(path);

        if (user == null) {
            presenter.present(new PersonalPlanOutputData(false, "You need an account", ""));
        } else if (!path.endsWith(".ics")) {
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

                String washroomList = processWashroomList(washrooms.getAll());
                if (Objects.isNull(washroomList)) {
                    throw new Exception("Washroom list processing error");
                }

                Schema washroom = Schema.builder().type(Type.Known.OBJECT).properties(Map.of(
                        DAY_PROMPT, Schema.builder().type(Type.Known.STRING).build(),
                        TIME_PROMPT, Schema.builder().type(Type.Known.STRING).build(),
                        WASHROOM_ID_PROMPT, Schema.builder().type(Type.Known.STRING).build()
                )).required(List.of(DAY_PROMPT, TIME_PROMPT, WASHROOM_ID_PROMPT)).build();

                Schema plan = Schema.builder().type(Type.Known.ARRAY).items(washroom).build();

                GenerateContentConfig config = GenerateContentConfig.builder().responseMimeType("application/json").responseSchema(plan).build();

                Client client = Client.builder().apiKey(apiKey).build();
                System.out.println("before asking gem");
                GenerateContentResponse response = client.models.generateContent("gemini-3.6-flash", "This is a UOFT time table, generate a schedule of washroom breaks in the " + inputData.semester() + " semester such that there are " + inputData.nTrips() + "washroom trips per day they are at school: " + calendar + "\nPlease only use washrooms from the following list and include only and exactly the washroom id in the washroom id field of the response: " + washroomList, config);
                String responseText = response.text();
                System.out.println(responseText);
                if (checkValid(response.text(), Integer.parseInt(inputData.nTrips()))) {
                    System.out.println("found not valid");
                    throw new Exception("Invalid gemini response");
                }
                String personalPlanString = convertPersonalPlan(responseText);
                if (Objects.isNull(personalPlanString)) {
                    System.out.println("later found suspicous");
                    throw new Exception("Invalid gemini response");
                }
                users.removeUser(user.username());
                User newUser = new User(user.username(), user.passwordHash(), personalPlanString);
                users.save(newUser);
                users.setCurrentUser(newUser);
                presenter.present(new PersonalPlanOutputData(true, "", personalPlanString));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                presenter.present(new PersonalPlanOutputData(false, "Gemini error, please try again", ""));
            }
        }

    }

    private boolean checkValid(String response, int n) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<WashroomPlan> washroomPlans = mapper.readValue(response, new TypeReference<List<WashroomPlan>>() {});
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            for (WashroomPlan washroomPlan : washroomPlans) {
                if (map.containsKey(washroomPlan.day)) {
                    map.put(washroomPlan.day, 1);
                } else {
                    map.put(washroomPlan.day, map.get(washroomPlan.day) + 1);
                }
                Washroom w = washrooms.getById(washroomPlan.washroom).orElse(null);
                if (Objects.isNull(w)) {
                    System.out.println("in check valid:" + washroomPlan.washroom);
                    throw new Exception("Gemini outputted invalid washroom");
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

    private String convertPersonalPlan(String plan) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<WashroomPlan> washroomPlans = mapper.readValue(plan, new TypeReference<List<WashroomPlan>>() {});
            List<HashMap<String, String>> washroomList = new ArrayList<>();
            for (WashroomPlan washroomPlan : washroomPlans) {
                Washroom w = washrooms.getById(washroomPlan.washroom).orElse(null);
                if (Objects.isNull(w)) {
                    System.out.println("in convert:" + washroomPlan.washroom);
                    throw new Exception("Gemini outputted invalid washroom");
                } else {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("day", washroomPlan.day);
                    map.put("time", washroomPlan.time);
                    map.put("id", washroomPlan.washroom);
                    map.put("name", w.name());
                    washroomList.add(map);
                }
            }

            return mapper.writeValueAsString(washroomList);

        } catch (Exception e) {
            return null;
        }

    }

    private String processWashroomList(List<Washroom> list) {

        List<HashMap<String, String>> washroomList = new ArrayList<>();
        for (Washroom washroom : list) {

            HashMap<String, String> map = new HashMap<>();
            map.put("id", washroom.id());
            map.put("name", washroom.name());
            map.put("building", washroom.building().name());
            map.put("gender", washroom.gender().name());
            washroomList.add(map);

        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(washroomList);
        } catch (Exception e) {
            System.out.println("in process");
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
        @JsonProperty(DAY_PROMPT)
        public String day;
        @JsonProperty(TIME_PROMPT)
        public String time;
        @JsonProperty(WASHROOM_ID_PROMPT)
        public String washroom;
    }

    public static class EntirePlan {
        public List<WashroomPlan> washrooms;
    }

}
