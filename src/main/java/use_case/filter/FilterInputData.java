package use_case.filter;

public record FilterInputData(
        float maxBusyness,
        float minCleanliness,
        boolean accessible,
        String gender,
        String washroomID,
        boolean ownReviews,
        double latitude,
        double longitude) {
};
