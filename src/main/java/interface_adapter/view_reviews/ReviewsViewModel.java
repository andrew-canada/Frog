package interface_adapter.view_reviews;

import interface_adapter.common.ViewModel;
import use_case.view_reviews.ViewReviewsOutputData;

import java.util.List;

public final class ReviewsViewModel extends ViewModel<ReviewsViewModel.State> {
    public ReviewsViewModel() {
        super(new State("", "", "", 0, 0, 0, 0, 0, List.of(), null));
    }

    public record State(String washroomId, String name, String subtitle, double rating, double cleanliness,
                        int reviewCount, int toilets, int sinks, List<ViewReviewsOutputData.ReviewDisplay> reviews,
                        String error) {
        public State {
            reviews = List.copyOf(reviews);
        }
    }
}
