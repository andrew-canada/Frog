package use_case.sort_washrooms;

import use_case.sort_review.SortReviewInputBoundary;

public record SortWashroomInputData(String sortBy, double lat, double lng) {
}
