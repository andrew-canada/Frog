package interface_adapter.filter;

import java.util.concurrent.CompletableFuture;

import entity.Washroom;
import use_case.filter.FilterInputBoundary;
import use_case.filter.FilterInputData;

public class FilterController {
    private static final int MAX_BUSYNESS_INDEX = 0;
    private static final int MIN_CLEANLINESS_INDEX = 1;
    private static final int ACCESSIBLE_INDEX = 2;
    private static final int OWN_REVIEWS_INDEX = 3;
    private static final int SELECTED_BUILDING_INDEX = 4;
    private static final int PERSONAL_PLAN_INDEX = 5;
    private static final int SELECTED_ID_INDEX = 6;
    private static final int GENDER_INDEX = 7;
    private static final int LATITUDE_INDEX = 8;
    private static final int LONGITUDE_INDEX = 9;
    private final FilterInputBoundary interactor;

    public FilterController(final FilterInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Performs this operation.
     *
     * @param values filter values in controller order.
     */
    public void execute(final Object... values) {
        final int maxBusyness = (Integer) values[MAX_BUSYNESS_INDEX];
        final int minCleanliness = (Integer) values[MIN_CLEANLINESS_INDEX];
        final boolean accessible = (Boolean) values[ACCESSIBLE_INDEX];
        final boolean ownReviews = (Boolean) values[OWN_REVIEWS_INDEX];
        final boolean selectedBuilding = (Boolean) values[SELECTED_BUILDING_INDEX];
        final boolean personalPlan = (Boolean) values[PERSONAL_PLAN_INDEX];
        final String selectedID = (String) values[SELECTED_ID_INDEX];
        final Washroom.Gender gender = (Washroom.Gender) values[GENDER_INDEX];
        final double latitude = ((Number) values[LATITUDE_INDEX]).doubleValue();
        final double longitude = ((Number) values[LONGITUDE_INDEX]).doubleValue();

        String inputID = "";
        if (selectedBuilding) {
            inputID = selectedID;
        }

        final String inputGender = switch (gender) {
            case Washroom.Gender.ALL_GENDER -> "ALL_GENDER";
            case Washroom.Gender.WOMEN -> "WOMEN";
            case Washroom.Gender.MEN -> "MEN";
            case null -> null;
        };

        final FilterInputData inputData =
            new FilterInputData((float) maxBusyness, (float) minCleanliness, accessible, inputGender, inputID,
                ownReviews, personalPlan, latitude, longitude);

        CompletableFuture.runAsync(() -> {
            interactor.execute(inputData);
        });
    }
}
