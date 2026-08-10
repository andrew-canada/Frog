package interface_adapter.view_reviews;

import java.util.List;

import interface_adapter.common.AbstractViewModel;
import use_case.view_reviews.ViewReviewsOutputData;

public final class ReviewsViewModel extends AbstractViewModel<ReviewsViewModel.State> {
    public ReviewsViewModel() {
        super(new State("", "", "", 0, 0, 0, 0, 0, List.of(), null));
    }

    /**
     * Updates the clicked review after a helpful vote without changing the
     * presentation order selected by the user.
     * @param reviewId parameter value.
     */
    public void toggleHelpfulVote(final String reviewId) {
        final State current = getState();
        final List<ViewReviewsOutputData.ReviewDisplay> updatedReviews = current
            .reviews()
            .stream()
            .map(review -> {
                if (review
                    .reviewId()
                    .equals(reviewId)) {
                    if (review.votedByCurrentUser()) {
                        return new ViewReviewsOutputData.ReviewDisplay(review.reviewId(), review.rating(),
                            review.comment(), Math.max(0, review.helpfulCount() + -1), review.date(), review.author(),
                            !review.votedByCurrentUser(), review.reportedByCurrentUser());
                    }
                    return new ViewReviewsOutputData.ReviewDisplay(review.reviewId(), review.rating(), review.comment(),
                        Math.max(0, review.helpfulCount() + 1), review.date(), review.author(),
                        !review.votedByCurrentUser(), review.reportedByCurrentUser());
                }
                else {
                    return review;
                }
            })
            .toList();
        setState(
            new State(current.washroomId(), current.name(), current.subtitle(), current.rating(), current.cleanliness(),
                current.reviewCount(), current.toilets(), current.sinks(), updatedReviews, current.error()));
    }

    public record State(String washroomId, String name, String subtitle, double rating, double cleanliness,
                        int reviewCount, int toilets, int sinks, List<ViewReviewsOutputData.ReviewDisplay> reviews,
                        String error) {
        public State {
            reviews = List.copyOf(reviews);
        }
    }
}
