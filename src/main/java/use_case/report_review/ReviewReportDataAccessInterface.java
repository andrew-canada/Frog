package use_case.report_review;

import entity.Report;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stores reports filed against reviews and answers whether a user has already
 * reported one.
 */
public interface ReviewReportDataAccessInterface {

    /**
     * Saves a new report.
     */
    void save(Report report);

    /**
     * @return whether the given user has already reported the given review.
     */
    boolean hasReported(String reviewId, String username);

    /**
     * Returns the requested reviews already reported by the user, preferably in one query.
     */
    default Set<String> reportedReviewIds(final Collection<String> reviewIds, final String username) {
        return reviewIds
            .stream()
            .filter(reviewId -> hasReported(reviewId, username))
            .collect(Collectors.toUnmodifiableSet());
    }

}
