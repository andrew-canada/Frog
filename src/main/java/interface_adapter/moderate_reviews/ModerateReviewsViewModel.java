package interface_adapter.moderate_reviews;

import interface_adapter.common.ViewModel;
import use_case.moderate_reviews.ReportedReview;

import java.util.List;

/** ViewModel for the moderator's Reported Reviews card. */
public final class ModerateReviewsViewModel extends ViewModel<ModerateReviewsViewModel.State> {

    public ModerateReviewsViewModel() {
        super(new State(List.of(), null));
    }

    public record State(List<ReportedReview> reportedReviews, String message) {
        public State {
            reportedReviews = List.copyOf(reportedReviews);
        }
    }
}
