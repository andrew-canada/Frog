package interface_adapter.view_reviews;

import java.util.List;

import interface_adapter.common.AbstractViewModel;
import use_case.view_reviews.ViewReviewsOutputData;

public final class ReviewsViewModel extends AbstractViewModel<ReviewsViewModel.State> {
    public ReviewsViewModel() {
        super(new State("", "", "", 0, 0, 0, 0, 0, List.of(), null));
    }

    /**
     * Updates the clicked review after a helpful vote without changing the presentation order selected by the user.
     *
     * @param reviewId identifier of the voted review.
     */
    public void toggleHelpfulVote(final String reviewId) {
        final State current = getState();
        final List<ViewReviewsOutputData.ReviewDisplay> updatedReviews = current
            .reviews()
            .stream()
            .map(review -> toggledReview(review, reviewId))
            .toList();
        setState(new State(current.washroomId(), current.name(), current.subtitle(), current.rating(),
            current.cleanliness(), current.reviewCount(), current.toilets(), current.sinks(), updatedReviews,
            current.error()));
    }

    private static ViewReviewsOutputData.ReviewDisplay toggledReview(
            final ViewReviewsOutputData.ReviewDisplay review, final String reviewId) {
        ViewReviewsOutputData.ReviewDisplay result = review;
        if (review.reviewId().equals(reviewId)) {
            final int change;
            if (review.votedByCurrentUser()) {
                change = -1;
            }
            else {
                change = 1;
            }
            result = new ViewReviewsOutputData.ReviewDisplay(review.reviewId(), review.rating(), review.comment(),
                Math.max(0, review.helpfulCount() + change), review.date(), review.author(),
                !review.votedByCurrentUser(), review.reportedByCurrentUser());
        }
        return result;
    }

    /** Holds the state displayed by the review screen. */
    public static final class State {
        private static final int VALUE_COUNT = 10;
        private static final int NAME_INDEX = 1;
        private static final int SUBTITLE_INDEX = 2;
        private static final int RATING_INDEX = 3;
        private static final int CLEANLINESS_INDEX = 4;
        private static final int REVIEW_COUNT_INDEX = 5;
        private static final int TOILETS_INDEX = 6;
        private static final int SINKS_INDEX = 7;
        private static final int REVIEWS_INDEX = 8;
        private static final int ERROR_INDEX = 9;

        private final String washroomId;
        private final String name;
        private final String subtitle;
        private final double rating;
        private final double cleanliness;
        private final int reviewCount;
        private final int toilets;
        private final int sinks;
        private final List<ViewReviewsOutputData.ReviewDisplay> reviews;
        private final String error;

        public State(final Object... values) {
            if (values.length != VALUE_COUNT) {
                throw new IllegalArgumentException("Review state requires ten values");
            }
            washroomId = (String) values[0];
            name = (String) values[NAME_INDEX];
            subtitle = (String) values[SUBTITLE_INDEX];
            rating = ((Number) values[RATING_INDEX]).doubleValue();
            cleanliness = ((Number) values[CLEANLINESS_INDEX]).doubleValue();
            reviewCount = ((Number) values[REVIEW_COUNT_INDEX]).intValue();
            toilets = ((Number) values[TOILETS_INDEX]).intValue();
            sinks = ((Number) values[SINKS_INDEX]).intValue();
            reviews = List.copyOf((List<ViewReviewsOutputData.ReviewDisplay>) values[REVIEWS_INDEX]);
            error = (String) values[ERROR_INDEX];
        }

        /**
         * Returns the washroom identifier.
         *
         * @return the washroom identifier.
         */
        public String washroomId() {
            return washroomId;
        }

        /**
         * Returns the building name.
         *
         * @return the building name.
         */
        public String name() {
            return name;
        }

        /**
         * Returns the descriptive subtitle.
         *
         * @return the descriptive subtitle.
         */
        public String subtitle() {
            return subtitle;
        }

        /**
         * Returns the average rating.
         *
         * @return the average rating.
         */
        public double rating() {
            return rating;
        }

        /**
         * Returns the average cleanliness.
         *
         * @return the average cleanliness.
         */
        public double cleanliness() {
            return cleanliness;
        }

        /**
         * Returns the number of reviews.
         *
         * @return the number of reviews.
         */
        public int reviewCount() {
            return reviewCount;
        }

        /**
         * Returns the number of toilets.
         *
         * @return the number of toilets.
         */
        public int toilets() {
            return toilets;
        }

        /**
         * Returns the number of sinks.
         *
         * @return the number of sinks.
         */
        public int sinks() {
            return sinks;
        }

        /**
         * Returns the displayed reviews.
         *
         * @return the displayed reviews.
         */
        public List<ViewReviewsOutputData.ReviewDisplay> reviews() {
            return reviews;
        }

        /**
         * Returns the current error message.
         *
         * @return the current error message.
         */
        public String error() {
            return error;
        }
    }
}
