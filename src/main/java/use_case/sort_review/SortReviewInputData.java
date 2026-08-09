package use_case.sort_review;

import entity.Washroom;
import entity.review.Review;

import java.util.Comparator;

public record SortReviewInputData(String sortBy, String currentWashroom) {

}
