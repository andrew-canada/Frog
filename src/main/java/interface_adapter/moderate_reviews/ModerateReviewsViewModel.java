package interface_adapter.moderate_reviews;

import java.util.List;

import interface_adapter.common.AbstractViewModel;
import use_case.moderate_reviews.ReportedReview;

/**
 * ViewModel for the moderator's Reported Reviews card.
 */
public final class ModerateReviewsViewModel extends AbstractViewModel<ModerateReviewsViewModel.State> {

    public ModerateReviewsViewModel() {
        super(new State(List.of(), null));
    }

    public record State(List<ReportedReview> reportedReviews, String message) {
        public State {
            reportedReviews = List.copyOf(reportedReviews);
        }
    }
}
