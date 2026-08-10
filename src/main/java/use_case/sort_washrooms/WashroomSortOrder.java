package use_case.sort_washrooms;

/**
 * Stable application sort options, independent of UI labels.
 */
public enum WashroomSortOrder {
    ALPHABETICAL, HIGHEST_RATED, NEAREST;

    /**
     * Performs this operation.
     *
     * @param label parameter value.
     *
     * @return the operation result.
     */
    public static WashroomSortOrder fromDisplayLabel(final String label) {
        return switch (label) {
            case "Highest Rated" -> HIGHEST_RATED;
            case "Nearest" -> NEAREST;
            case "Alphabetical" -> ALPHABETICAL;
            case null -> ALPHABETICAL;
            default -> ALPHABETICAL;
        };
    }
}
