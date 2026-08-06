package interface_adapter.filter;

import entity.Washroom;
import use_case.filter.FilterInputBoundary;
import use_case.filter.FilterInputData;


public class FilterController {
    private final FilterInputBoundary interactor;

    public FilterController(FilterInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(
            int minRating,
            int minCleanliness,
            boolean accessible,
            boolean ownReviews,
            boolean selectedBuilding,
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
                (float) minRating,
                5.0F,
                (float) minCleanliness,
                5.0F,
                accessible,
                inputGender,
                inputID,
                ownReviews,
                latitude,
                longitude);

        interactor.execute(inputData);
    }
}
