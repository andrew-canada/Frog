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
    private static final int MAX_RATING_LEVEL = 5;
    private final ReviewRepository reviews;
    private final WriteReviewOutputBoundary presenter;
    private final Clock clock;

    public WriteReviewInteractor(final ReviewRepository reviews, final WriteReviewOutputBoundary presenter) {
        this(reviews, presenter, Clock.systemDefaultZone());
    }

    private WriteReviewInteractor(final ReviewRepository reviews, final WriteReviewOutputBoundary presenter,
                                  final Clock clock) {
        this.reviews = reviews;
        this.presenter = presenter;
        this.clock = clock;
    }

    @Override
    public void execute(final WriteReviewInputData input) {
        final String validationMessage = validationMessage(input);
        if (validationMessage != null) {
            presenter.present(new WriteReviewOutputData(false, validationMessage));
        }
        else {
            final String comment;
            if (input.comment() == null) {
                comment = "";
            }
            else {
                comment = input.comment().trim();
            }
            final String username;
            if (input.username() == null || input.username().isBlank()) {
                username = "Anonymous";
            }
            else {
                username = input.username();
            }
            reviews.save(new Review(UUID
                .randomUUID()
                .toString(), input.washroomId(), username, input.rating(), input.cleanliness(), comment, 0,
                LocalDate.now(clock)));
            presenter.present(new WriteReviewOutputData(true, "Thanks - your review was posted."));
        }
    }

    private static String validationMessage(final WriteReviewInputData input) {
        String result = null;
        if (input.washroomId() == null || input.washroomId().isBlank()) {
            result = "Choose a washroom before writing a review.";
        }
        else if (input.rating() < 1 || input.rating() > MAX_RATING_LEVEL
                || input.cleanliness() < 1 || input.cleanliness() > MAX_RATING_LEVEL) {
            result = "Ratings must be between 1 and 5.";
        }
        else if (input.comment() == null || input.comment().trim().isBlank()) {
            result = "Write a short review before submitting.";
        }
        return result;
    }
}
