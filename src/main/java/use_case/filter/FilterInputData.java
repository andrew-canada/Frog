package use_case.filter;

import entity.Building;

import java.util.List;

public record FilterInputData(
        float minRating,
        float maxRating,
        float minCleanliness,
        float maxCleanliness,
        boolean accessible,
        String gender,
        String washroomID,
        boolean ownReviews,
        double latitude,
        double longitude) {
};
