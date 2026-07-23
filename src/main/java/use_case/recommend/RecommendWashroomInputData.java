package use_case.recommend;

import entity.Washroom;

public record RecommendWashroomInputData(double latitude, double longitude, boolean accessibleOnly,
        Washroom.Gender gender, boolean inAHurry, String username) { }
