package interface_adapter.view_reviews;

import interface_adapter.common.ViewModel;
import use_case.view_reviews.ViewReviewsOutputData;

import java.util.List;

public final class ReviewsViewModel extends ViewModel<ReviewsViewModel.State> {
    public ReviewsViewModel() {
        super(new State("", "", "", 0, 0, 0, 0, 0, List.of(), null));
    }

    /**
     * Updates the clicked review after a helpful vote without changing the
     * presentation order selected by the user.
     */
    public void toggleHelpfulVote(String reviewId) {
        State current = getState();
        List<ViewReviewsOutputData.ReviewDisplay> updatedReviews = current.reviews().stream()
                .map(review -> review.reviewId().equals(reviewId)
                        ? new ViewReviewsOutputData.ReviewDisplay(
                        review.reviewId(), review.rating(), review.comment(),
                        Math.max(0, review.helpfulCount() + (review.votedByCurrentUser() ? -1 : 1)),
                        review.date(), review.author(), !review.votedByCurrentUser(),
                        review.reportedByCurrentUser())
                        : review)
                .toList();
        setState(new State(current.washroomId(), current.name(), current.subtitle(), current.rating(),
                current.cleanliness(), current.reviewCount(), current.toilets(), current.sinks(),
                updatedReviews, current.error()));
    }

    public record State(String washroomId, String name, String subtitle, double rating, double cleanliness,
                        int reviewCount, int toilets, int sinks, List<ViewReviewsOutputData.ReviewDisplay> reviews,
                        String error) {
        public State {
            reviews = List.copyOf(reviews);
        }
    }
}
