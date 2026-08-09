package use_case.write_review;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import entity.Review;
import use_case.port.ReviewRepository;

/**
 * Validates and stores a review through the same repository used to display it.
 */
public final class WriteReviewInteractor implements WriteReviewInputBoundary {
    private final ReviewRepository reviews;
    private final WriteReviewOutputBoundary presenter;
    private final Clock clock;

    public WriteReviewInteractor(final ReviewRepository reviews, final WriteReviewOutputBoundary presenter) {
        this(reviews, presenter, Clock.systemDefaultZone());
    }

    public WriteReviewInteractor(final ReviewRepository reviews, final WriteReviewOutputBoundary presenter,
                                 final Clock clock) {
        this.reviews = reviews;
        this.presenter = presenter;
        this.clock = clock;
    }

    @Override
    public void execute(final WriteReviewInputData input) {
        if (input.washroomId() == null || input
            .washroomId()
            .isBlank()) {
            presenter.present(new WriteReviewOutputData(false, "Choose a washroom before writing a review."));
            return;
        }
        if (input.rating() < 1 || input.rating() > 5 || input.cleanliness() < 1 || input.cleanliness() > 5) {
            presenter.present(new WriteReviewOutputData(false, "Ratings must be between 1 and 5."));
            return;
        }
        final String comment = input.comment() == null ? "" : input
                                                              .comment()
                                                              .trim();
        if (comment.isBlank()) {
            presenter.present(new WriteReviewOutputData(false, "Write a short review before submitting."));
            return;
        }
        final String username = input.username() == null || input
            .username()
            .isBlank() ? "Anonymous" : input.username();
        reviews.save(new Review(UUID
            .randomUUID()
            .toString(), input.washroomId(), username, input.rating(), input.cleanliness(), comment, 0,
            LocalDate.now(clock)));
        presenter.present(new WriteReviewOutputData(true, "Thanks — your review was posted."));
    }
}
