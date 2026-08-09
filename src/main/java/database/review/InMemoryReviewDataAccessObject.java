package database.review;

import entity.Report;
import entity.Review;
import entity.ReviewSummary;
import use_case.moderate_reviews.ReportedReviewsDataAccessInterface;
import use_case.moderate_reviews.ReviewAdminDataAccessInterface;
import use_case.report_review.ReviewReportDataAccessInterface;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;
import use_case.port.ReviewRepository;

import java.time.LocalDate;
import java.util.*;

public final class InMemoryReviewDataAccessObject implements ReviewRepository,
        HelpfulVoteDataAccessInterface, ReviewReportDataAccessInterface, ReviewAdminDataAccessInterface, ReportedReviewsDataAccessInterface {
    private final List<Review> reviews = new ArrayList<>();
    private final Map<String, Set<String>> votesByReview = new HashMap<>();
    private final List<Report> reports = new ArrayList<>();

    public InMemoryReviewDataAccessObject() {
        reviews.add(new Review("r1", "bahen-2", "sheena_q", 5, 5,
                "Spotless and rarely busy. Good lighting and a spacious accessible stall.", 14, LocalDate.of(2026, 3, 12)));
        reviews.add(new Review("r2", "bahen-2", "andrew_p", 4, 4,
                "Clean most days but can get crowded between classes. Soap was full.", 6, LocalDate.of(2026, 2, 28)));
        reviews.add(new Review("r3", "robarts-4", "eleanor_l", 4, 4,
                "Reliable and easy to find, though busy after lunch.", 8, LocalDate.of(2026, 4, 3)));
        reviews.add(new Review("r4", "gerstein-main", "ian_c", 3.5, 3,
                "Quiet in the morning. One sink was out of service.", 4, LocalDate.of(2026, 4, 16)));
    }

    public InMemoryReviewDataAccessObject(List<Review> seed) {
        reviews.addAll(seed);
    }

    // --- View Reviews ----------------------------------------------------------
    @Override
    public List<Review> getReviewsForWashroom(String id) {
        return reviews.stream().filter(r -> r.washroomId().equals(id)).toList();
    }

    @Override
    public ReviewSummary getSummary(String id) {
        List<Review> found = getReviewsForWashroom(id);
        if (found.isEmpty()) return ReviewSummary.empty();
        return new ReviewSummary(found.stream().mapToDouble(Review::rating).average().orElse(0),
                found.stream().mapToDouble(Review::cleanliness).average().orElse(0), found.size());
    }

    @Override
    public List<Review> getReviewsByUser(String username) {
        return reviews.stream().filter(r -> username.equals(r.authorUsername())).toList();
    }

    @Override
    public void save(Review review) {
        reviews.add(review);
    }

    // --- Helpful votes ---------------------------------------------------------
    @Override
    public boolean hasVoted(String reviewId, String username) {
        return votesByReview.getOrDefault(reviewId, Set.of()).contains(username);
    }

    @Override
    public Set<String> votedReviewIds(Collection<String> reviewIds, String username) {
        return reviewIds.stream().filter(reviewId -> hasVoted(reviewId, username)).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public void addVote(String reviewId, String username) {
        if (votesByReview.computeIfAbsent(reviewId, key -> new HashSet<>()).add(username)) {
            adjustHelpful(reviewId, +1);
        }
    }

    @Override
    public void removeVote(String reviewId, String username) {
        if (votesByReview.getOrDefault(reviewId, Set.of()).remove(username)) {
            adjustHelpful(reviewId, -1);
        }
    }

    private void adjustHelpful(String reviewId, int delta) {
        for (int i = 0; i < reviews.size(); i++) {
            Review r = reviews.get(i);
            if (r.id().equals(reviewId)) {
                int count = Math.max(0, r.helpfulCount() + delta);
                reviews.set(i, new Review(r.id(), r.washroomId(), r.authorUsername(), r.rating(),
                        r.cleanliness(), r.comment(), count, r.createdAt()));
                return;
            }
        }
    }

    // --- Reports ---------------------------------------------------------------
    @Override
    public void save(Report report) {
        reports.add(report);
    }

    @Override
    public boolean hasReported(String reviewId, String username) {
        return reports.stream()
                .anyMatch(report -> report.reviewId().equals(reviewId) && username.equals(report.reporterUsername()));
    }

    @Override
    public Set<String> reportedReviewIds(Collection<String> reviewIds, String username) {
        return reports.stream().filter(report -> reviewIds.contains(report.reviewId()))
                .filter(report -> username.equals(report.reporterUsername())).map(Report::reviewId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public List<Report> getAllReports() {
        return List.copyOf(reports);
    }

    @Override
    public void deleteReportsForReview(String reviewId) {
        reports.removeIf(report -> report.reviewId().equals(reviewId));
    }

    // --- Review admin ----------------------------------------------------------
    @Override
    public Optional<Review> getById(String reviewId) {
        return reviews.stream().filter(r -> r.id().equals(reviewId)).findFirst();
    }

    @Override
    public void deleteReview(String reviewId) {
        reviews.removeIf(r -> r.id().equals(reviewId));
        votesByReview.remove(reviewId);
    }
}
