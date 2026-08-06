package use_case.moderate_reviews;

import entity.Report;

import java.util.List;

/**
 * Read and clear access to filed reports, as needed by moderation to build the
 * reported-review queue and to clear a review's reports.
 */
public interface ReportedReviewsDataAccessInterface {

    /**
     * @return every report currently on file (across all reviews).
     */
    List<Report> getAllReports();

    /**
     * Removes all reports against a review (used when reports are dismissed or the review is removed).
     */
    void deleteReportsForReview(String reviewId);
}
