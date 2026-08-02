package interface_adapter.recommend;

import entity.Washroom;
import use_case.recommend.RecommendWashroomInputBoundary;
import use_case.recommend.RecommendWashroomInputData;

public final class RecommendationController {
    private final RecommendWashroomInputBoundary interactor;

    public RecommendationController(RecommendWashroomInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(double lat, double lng, boolean accessible, Washroom.Gender gender, boolean hurry, String username) {
        interactor.execute(new RecommendWashroomInputData(lat, lng, accessible, gender, hurry, username));
    }
}
