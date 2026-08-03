package use_case.vote_helpful;

/**
 * Ranks reviews by combining helpfulness and recency.
 * <p/>
 * score = ln(1 + helpfulVotes) + 1.5 * e^(-ageInDays / 30)
 */
public final class ReviewScorer {

    private static final double RECENCY_WEIGHT = 1.5;
    private static final double RECENCY_HALF_LIFE_DAYS = 30.0;

    private ReviewScorer() {
    }

    public static double score(int helpfulVotes, long ageInDays) {
        return Math.log(1 + helpfulVotes)
                + RECENCY_WEIGHT * Math.exp(-ageInDays / RECENCY_HALF_LIFE_DAYS);
    }
}
