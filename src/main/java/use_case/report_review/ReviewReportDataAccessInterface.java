package use_case.report_review;

import entity.Report;

/**
 * Stores reports filed against reviews and answers whether a user has already
 * reported one.
 */
public interface ReviewReportDataAccessInterface {

    /** Saves a new report. */
    void save(Report report);

    /** @return whether the given user has already reported the given review. */
    boolean hasReported(String reviewId, String username);

}
