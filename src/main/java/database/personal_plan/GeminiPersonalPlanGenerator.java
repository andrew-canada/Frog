package database.personal_plan;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import entity.Washroom;
import use_case.account.personal_plan.PersonalPlanGenerator;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Gemini implementation of the personal-plan generator boundary. */
public final class GeminiPersonalPlanGenerator implements PersonalPlanGenerator {
    public static final String API_KEY_ENV = "GEMINI_API_KEY";
    private static final String DAY_PROMPT = "Day of week";
    private static final String TIME_PROMPT = "Time (nearest hour) of washroom break";
    private static final String WASHROOM_ID_PROMPT = "Washroom id";

    private final Supplier<String> apiKey;

    public GeminiPersonalPlanGenerator(Supplier<String> apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String generate(String calendarContent, int tripsPerDay, String semester,
                           List<Washroom> availableWashrooms) {
        String key = apiKey.get();
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Set " + API_KEY_ENV + " to generate a personal plan");
        }
        Schema washroomSchema = Schema.builder().type(Type.Known.OBJECT).properties(Map.of(
                DAY_PROMPT, Schema.builder().type(Type.Known.STRING).build(),
                TIME_PROMPT, Schema.builder().type(Type.Known.STRING).build(),
                WASHROOM_ID_PROMPT, Schema.builder().type(Type.Known.STRING).build()
        )).required(List.of(DAY_PROMPT, TIME_PROMPT, WASHROOM_ID_PROMPT)).build();
        Schema plan = Schema.builder().type(Type.Known.ARRAY).items(washroomSchema).build();
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseMimeType("application/json").responseSchema(plan).build();
        Client client = Client.builder().apiKey(key).build();
        String washroomChoices = availableWashrooms.stream()
                .map(washroom -> washroom.id() + " — " + washroom.name() + " (" + washroom.building().name() + ")")
                .collect(Collectors.joining("\n"));
        GenerateContentResponse response = client.models.generateContent("gemini-3.6-flash",
                "This is a UOFT timetable. Generate a " + semester + " semester schedule with " + tripsPerDay
                        + " washroom breaks per day the student is on campus. Use only the exact washroom IDs below in the "
                        + WASHROOM_ID_PROMPT + " field.\nTimetable:\n" + calendarContent
                        + "\nAllowed washrooms:\n" + washroomChoices, config);
        return response.text();
    }
}
