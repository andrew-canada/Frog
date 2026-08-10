package use_case.sort_review;

/**
 * Stable application sort options, independent of UI labels.
 */
public enum ReviewSortOrder {
    RELEVANCE, MOST_HELPFUL, HIGHEST_RATED, LOWEST_RATED, NEWEST, VOTED_BY_ME;

    /**
     * Performs this operation.
     *
     * @param label parameter value.
     *
     * @return the operation result.
     */
    public static ReviewSortOrder fromDisplayLabel(final String label) {
        return switch (label) {
            case "Most Helpful" -> MOST_HELPFUL;
            case "Highest Rated" -> HIGHEST_RATED;
            case "Lowest Rated" -> LOWEST_RATED;
            case "Newest" -> NEWEST;
            case "Voted by Me" -> VOTED_BY_ME;
            case "Relevant" -> RELEVANCE;
            case null -> RELEVANCE;
            default -> RELEVANCE;
        };
    }
}
