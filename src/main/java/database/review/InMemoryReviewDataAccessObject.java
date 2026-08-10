package database.review;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import entity.Report;
import entity.Review;
import entity.ReviewSummary;
import use_case.moderate_reviews.ReportedReviewsDataAccessInterface;
import use_case.moderate_reviews.ReviewAdminDataAccessInterface;
import use_case.port.ReviewRepository;
import use_case.report_review.ReviewReportDataAccessInterface;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;

public final class InMemoryReviewDataAccessObject
    implements ReviewRepository, HelpfulVoteDataAccessInterface, ReviewReportDataAccessInterface,
    ReviewAdminDataAccessInterface, ReportedReviewsDataAccessInterface {
    private static final int SAMPLE_REVIEW_DAY_SIXTEEN = 16;
    private static final int SAMPLE_REVIEW_VALUE_FOUR = 4;
    private static final int SAMPLE_REVIEW_YEAR = 2026;
    private static final int SAMPLE_REVIEW_VALUE_THREE = 3;
    private static final double SAMPLE_REVIEW_RATING_THREE_POINT_FIVE = 3.5;
    private static final int SAMPLE_REVIEW_HELPFUL_COUNT_EIGHT = 8;
    private static final int SAMPLE_REVIEW_DAY_TWENTY_EIGHT = 28;
    private static final int SAMPLE_REVIEW_HELPFUL_COUNT_SIX = 6;
    private static final int SAMPLE_REVIEW_DAY_TWELVE = 12;
    private static final int SAMPLE_REVIEW_HELPFUL_COUNT_FOURTEEN = 14;
    private static final int SAMPLE_REVIEW_VALUE_FIVE = 5;
    private final List<Review> reviews = new ArrayList<>();
    private final Map<String, Set<String>> votesByReview = new HashMap<>();
    private final List<Report> reports = new ArrayList<>();

    public InMemoryReviewDataAccessObject() {
        reviews.add(new Review("r1", "bahen-2", "sheena_q", SAMPLE_REVIEW_VALUE_FIVE,
            SAMPLE_REVIEW_VALUE_FIVE, "Spotless and rarely busy. Good lighting and a spacious accessible stall.",
            SAMPLE_REVIEW_HELPFUL_COUNT_FOURTEEN,
            LocalDate.of(SAMPLE_REVIEW_YEAR, SAMPLE_REVIEW_VALUE_THREE, SAMPLE_REVIEW_DAY_TWELVE)));
        reviews.add(new Review("r2", "bahen-2", "andrew_p", SAMPLE_REVIEW_VALUE_FOUR,
            SAMPLE_REVIEW_VALUE_FOUR, "Clean most days but can get crowded between classes. Soap was full.",
            SAMPLE_REVIEW_HELPFUL_COUNT_SIX,
            LocalDate.of(SAMPLE_REVIEW_YEAR, 2, SAMPLE_REVIEW_DAY_TWENTY_EIGHT)));
        reviews.add(
            new Review("r3", "robarts-4", "eleanor_l", SAMPLE_REVIEW_VALUE_FOUR, SAMPLE_REVIEW_VALUE_FOUR,
                "Reliable and easy to find, though busy after lunch.", SAMPLE_REVIEW_HELPFUL_COUNT_EIGHT,
                LocalDate.of(SAMPLE_REVIEW_YEAR, SAMPLE_REVIEW_VALUE_FOUR, SAMPLE_REVIEW_VALUE_THREE)));
        reviews.add(
            new Review("r4", "gerstein-main", "ian_c", SAMPLE_REVIEW_RATING_THREE_POINT_FIVE,
                SAMPLE_REVIEW_VALUE_THREE,
                "Quiet in the morning. One sink was out of service.", SAMPLE_REVIEW_VALUE_FOUR,
                LocalDate.of(SAMPLE_REVIEW_YEAR, SAMPLE_REVIEW_VALUE_FOUR, SAMPLE_REVIEW_DAY_SIXTEEN)));
    }

    public InMemoryReviewDataAccessObject(final List<Review> seed) {
        reviews.addAll(seed);
    }

    // --- View Reviews ----------------------------------------------------------

    @Override
    public List<Review> getReviewsForWashroom(final String id) {
        return reviews
            .stream()
            .filter(reviewValue -> {
                return reviewValue
                    .washroomId()
                    .equals(id);
            })
            .toList();
    }

    @Override
    public ReviewSummary getSummary(final String id) {
        final List<Review> found = getReviewsForWashroom(id);
        final ReviewSummary result;
        if (found.isEmpty()) {
            result = ReviewSummary.empty();
        }
        else {
            result = new ReviewSummary(found
                .stream()
                .mapToDouble(Review::rating)
                .average()
                .orElse(0), found
                .stream()
                .mapToDouble(Review::cleanliness)
                .average()
                .orElse(0), found.size());
        }
        return result;
    }

    @Override
    public List<Review> getReviewsByUser(final String username) {
        return reviews
            .stream()
            .filter(reviewValue -> {
                return username.equals(reviewValue.authorUsername());
            })
            .toList();
    }

    @Override
    public void save(final Review review) {
        reviews.add(review);
    }

    @Override
    public void save(final Report report) {
        reports.add(report);
    }

    // --- Helpful votes ---------------------------------------------------------

    @Override
    public boolean hasVoted(final String reviewId, final String username) {
        return votesByReview
            .getOrDefault(reviewId, Set.of())
            .contains(username);
    }

    @Override
    public Set<String> votedReviewIds(final Collection<String> reviewIds, final String username) {
        return reviewIds
            .stream()
            .filter(reviewId -> {
                return hasVoted(reviewId, username);
            })
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public void addVote(final String reviewId, final String username) {
        if (votesByReview
            .computeIfAbsent(reviewId, key -> {
                return new HashSet<>();
            })
            .add(username)) {
            adjustHelpful(reviewId, +1);
        }
    }

    @Override
    public void removeVote(final String reviewId, final String username) {
        if (votesByReview
            .getOrDefault(reviewId, Set.of())
            .remove(username)) {
            adjustHelpful(reviewId, -1);
        }
    }

    private void adjustHelpful(final String reviewId, final int delta) {
        for (int i = 0; i < reviews.size(); i++) {
            final Review reviewValue = reviews.get(i);
            if (reviewValue
                .id()
                .equals(reviewId)) {
                final int count = Math.max(0, reviewValue.helpfulCount() + delta);
                reviews.set(i,
                    new Review(reviewValue.id(), reviewValue.washroomId(), reviewValue.authorUsername(),
                        reviewValue.rating(), reviewValue.cleanliness(), reviewValue.comment(),
                        count, reviewValue.createdAt()));
                break;
            }
        }
    }

    // --- Reports ---------------------------------------------------------------

    @Override
    public boolean hasReported(final String reviewId, final String username) {
        return reports
            .stream()
            .anyMatch(report -> {
                return report
                    .reviewId()
                    .equals(reviewId) && username.equals(report.reporterUsername());
            });
    }

    @Override
    public Set<String> reportedReviewIds(final Collection<String> reviewIds, final String username) {
        return reports
            .stream()
            .filter(report -> {
                return reviewIds.contains(report.reviewId());
            })
            .filter(report -> {
                return username.equals(report.reporterUsername());
            })
            .map(Report::reviewId)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public List<Report> getAllReports() {
        return List.copyOf(reports);
    }

    @Override
    public void deleteReportsForReview(final String reviewId) {
        reports.removeIf(report -> {
            return report
                .reviewId()
                .equals(reviewId);
        });
    }

    // --- Review admin ----------------------------------------------------------

    @Override
    public Optional<Review> getById(final String reviewId) {
        return reviews
            .stream()
            .filter(reviewValue -> {
                return reviewValue
                    .id()
                    .equals(reviewId);
            })
            .findFirst();
    }

    @Override
    public void deleteReview(final String reviewId) {
        reviews.removeIf(reviewValue -> {
            return reviewValue
                .id()
                .equals(reviewId);
        });
        votesByReview.remove(reviewId);
    }
}
