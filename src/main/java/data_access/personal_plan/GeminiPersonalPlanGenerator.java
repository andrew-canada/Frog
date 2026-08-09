package data_access.personal_plan;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import use_case.account.personal_plan.PersonalPlanGenerator;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Gemini implementation of the personal-plan generator boundary. */
public final class GeminiPersonalPlanGenerator implements PersonalPlanGenerator {
    public static final String API_KEY_ENV = "GEMINI_API_KEY";
    private static final String DAY_PROMPT = "Day of week";
    private static final String TIME_PROMPT = "Time (nearest hour) of washroom break";
    private static final String WASHROOM_PROMPT = "Washroom";

    private final Supplier<String> apiKey;

    public GeminiPersonalPlanGenerator(Supplier<String> apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String generate(String calendarContent, int tripsPerDay) {
        String key = apiKey.get();
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Set " + API_KEY_ENV + " to generate a personal plan");
        }
        Schema washroom = Schema.builder().type(Type.Known.OBJECT).properties(Map.of(
                DAY_PROMPT, Schema.builder().type(Type.Known.STRING).build(),
                TIME_PROMPT, Schema.builder().type(Type.Known.STRING).build(),
                WASHROOM_PROMPT, Schema.builder().type(Type.Known.STRING).build()
        )).required(List.of(DAY_PROMPT, TIME_PROMPT, WASHROOM_PROMPT)).build();
        Schema plan = Schema.builder().type(Type.Known.ARRAY).items(washroom).build();
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseMimeType("application/json").responseSchema(plan).build();
        Client client = Client.builder().apiKey(key).build();
        GenerateContentResponse response = client.models.generateContent("gemini-3.6-flash",
                "This is a UOFT time table, generate a schedule of washroom breaks in the fall semester such that there are "
                        + tripsPerDay + " washroom trips per day they are at school: " + calendarContent, config);
        return response.text();
    }
}
