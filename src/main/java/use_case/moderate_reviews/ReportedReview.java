package use_case.moderate_reviews;

import java.time.LocalDate;
import java.util.List;

/**
 * A review that has outstanding reports, aggregated for the moderator queue.
 */
public record ReportedReview(String reviewId, String washroomName, String author, LocalDate date,
                             double rating, String comment, List<ReasonCount> reasonCounts,
                             List<AdditionalDetail> additionalDetails) {

    public ReportedReview {
        reasonCounts = List.copyOf(reasonCounts);
        additionalDetails = List.copyOf(additionalDetails);
    }

    /**
     * @return total number of reports (sum of the reason counts).
     */
    public int totalReports() {
        return reasonCounts.stream().mapToInt(ReasonCount::count).sum();
    }

    /**
     * @return how many reports include written details (drives the "(N)" button).
     */
    public int additionalDetailsCount() {
        return additionalDetails.size();
    }

    /**
     * A report reason and how many reports cited it.
     */
    public record ReasonCount(String reason, int count) {
    }

    /**
     * A single report's written explanation and the reason it was filed under.
     */
    public record AdditionalDetail(String reason, String text) {
    }
}
