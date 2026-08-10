package use_case.moderate_reviews;

import java.util.List;

import entity.Report;

/**
 * Read and clear access to filed reports, as needed by moderation to build the
 * reported-review queue and to clear a review's reports.
 */
public interface ReportedReviewsDataAccessInterface {

    /**
     * Returns every report currently on file (across all reviews).
     *
     * @return every report currently on file (across all reviews).
     */
    List<Report> getAllReports();

    /**
     * Removes all reports against a review (used when reports are dismissed or the review is removed).
     * @param reviewId parameter value.
     */
    void deleteReportsForReview(String reviewId);
}
