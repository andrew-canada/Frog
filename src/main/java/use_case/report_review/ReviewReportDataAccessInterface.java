package use_case.report_review;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import entity.Report;

/**
 * Stores reports filed against reviews and answers whether a user has already
 * reported one.
 */
public interface ReviewReportDataAccessInterface {

    /**
     * Saves a new report.
     * @param report parameter value.
     */
    void save(Report report);

    /**
     * Checks whether a user has already reported a review.
     * @param reviewId parameter value.
     * @param username parameter value.
     * @return whether the given user has already reported the given review.
     */
    boolean hasReported(String reviewId, String username);

    /**
     * Returns the requested reviews already reported by the user, preferably in one query.
     * @param reviewIds parameter value.
     * @param username parameter value.
     * @return the operation result.
     */
    default Set<String> reportedReviewIds(final Collection<String> reviewIds, final String username) {
        return reviewIds
            .stream()
            .filter(reviewId -> {
                return hasReported(reviewId, username);
            })
            .collect(Collectors.toUnmodifiableSet());
    }

}
