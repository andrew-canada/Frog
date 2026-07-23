package use_case.recommend;

import java.util.Map;

public record RecommendWashroomOutputData(String washroomId, String name, double rating,
        int distanceMeters, double currentBusyness, boolean accessible, String gender,
        double totalScore, Map<String, Double> scoreBreakdown) {
    public RecommendWashroomOutputData { scoreBreakdown = Map.copyOf(scoreBreakdown); }
}
