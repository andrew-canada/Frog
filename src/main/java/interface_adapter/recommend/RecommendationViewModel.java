package interface_adapter.recommend;

import interface_adapter.common.ViewModel;

import java.util.Map;

public final class RecommendationViewModel extends ViewModel<RecommendationViewModel.State> {
    public RecommendationViewModel() {
        super(new State("", "", 0, 0, 0, false, "", 0, Map.of(), null));
    }

    public record State(String washroomId, String name, double rating, int distanceMeters, double busyness,
                        boolean accessible, String gender, double score, Map<String, Double> reasons, String error) {
        public State {
            reasons = Map.copyOf(reasons);
        }
    }
}
