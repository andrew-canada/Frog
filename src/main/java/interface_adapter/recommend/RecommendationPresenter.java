package interface_adapter.recommend;

import use_case.recommend.RecommendWashroomOutputBoundary;
import use_case.recommend.RecommendWashroomOutputData;

import java.util.Map;

public final class RecommendationPresenter implements RecommendWashroomOutputBoundary {
    private final RecommendationViewModel model;

    public RecommendationPresenter(RecommendationViewModel model) {
        this.model = model;
    }

    @Override
    public void present(RecommendWashroomOutputData d) {
        model.setState(new RecommendationViewModel.State(
                d.washroomId(), d.name(), d.rating(), d.distanceMeters(), d.currentBusyness(), d.accessible(), d.gender(), d.totalScore(), d.scoreBreakdown(), null));
    }

    @Override
    public void presentError(String m) {
        model.setState(new RecommendationViewModel.State("", "", 0, 0, 0, false, "", 0, Map.of(), m));
    }
}
