package use_case.filter;

import entity.building.Building;

import java.util.List;

public record FilterInputData(
        float minRating,
        float maxRating,
        float minCleanliness,
        float maxCleanliness,
        boolean accessible,
        List<String> genders,
        Building building,
        boolean ownReviews) {};
