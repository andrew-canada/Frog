package interface_adapter.filter;

import java.util.concurrent.CompletableFuture;

import entity.Washroom;
import use_case.filter.FilterInputBoundary;
import use_case.filter.FilterInputData;

public class FilterController {
    private final FilterInputBoundary interactor;

    public FilterController(final FilterInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Performs this operation.
     *
     * @param accessible parameter value.
     *
     * @param gender parameter value.
     *
     * @param latitude parameter value.
     *
     * @param longitude parameter value.
     *
     * @param maxBusyness parameter value.
     *
     * @param minCleanliness parameter value.
     *
     * @param ownReviews parameter value.
     *
     * @param personalPlan parameter value.
     *
     * @param selectedBuilding parameter value.
     *
     * @param selectedID parameter value.
     */
    public void execute(final int maxBusyness, final int minCleanliness, final boolean accessible,
                        final boolean ownReviews, final boolean selectedBuilding, final boolean personalPlan,
                        final String selectedID, final Washroom.Gender gender, final double latitude,
                        final double longitude) {

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
