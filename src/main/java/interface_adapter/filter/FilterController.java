package interface_adapter.filter;

import entity.Washroom;
import use_case.filter.FilterInputBoundary;
import use_case.filter.FilterInputData;

import java.util.concurrent.CompletableFuture;

public class FilterController {
    private final FilterInputBoundary interactor;

    public FilterController(FilterInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(
            int maxBusyness,
            int minCleanliness,
            boolean accessible,
            boolean ownReviews,
            boolean selectedBuilding,
            boolean personalPlan,
            String selectedID,
            Washroom.Gender gender,
            double latitude,
            double longitude) {

        String inputID = "";
        if (selectedBuilding) {
            inputID = selectedID;
        }

        String inputGender = switch (gender) {
            case Washroom.Gender.ALL_GENDER -> "ALL_GENDER";
            case Washroom.Gender.WOMEN -> "WOMEN";
            case Washroom.Gender.MEN -> "MEN";
            case null -> null;
        };

        FilterInputData inputData = new FilterInputData(
                (float) maxBusyness,
                (float) minCleanliness,
                accessible,
                inputGender,
                inputID,
                ownReviews,
                personalPlan,
                latitude,
                longitude);

        CompletableFuture.runAsync(() -> interactor.execute(inputData));
    }
}
