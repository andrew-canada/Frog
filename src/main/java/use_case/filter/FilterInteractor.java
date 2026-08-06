package use_case.filter;

import data_access.AbstractCondition;
import data_access.CollectionCondition;
import data_access.Condition;
import data_access.Operator;
import data_access.user.UserDataAccessInterface;
import entity.Building;
import entity.Review;
import entity.User;
import entity.Washroom;
import data_access.washroom.WashroomDataAccessInterface;
import use_case.view_reviews.ReviewDataAccessInterface;

import java.util.*;
import java.util.function.Predicate;

public class FilterInteractor implements FilterInputBoundary {
    WashroomDataAccessInterface washroomDAO;
    ReviewDataAccessInterface reviewDAO;
    UserDataAccessInterface userDAO;
    FilterOutputBoundary presenter;

    public FilterInteractor(WashroomDataAccessInterface washroomDAO,
                            ReviewDataAccessInterface reviewDAO,
                            UserDataAccessInterface userDAO,
                            FilterOutputBoundary presenter) {
        this.washroomDAO = washroomDAO;
        this.reviewDAO = reviewDAO;
        this.userDAO = userDAO;
        this.presenter = presenter;
    }

    @Override
    public void execute(FilterInputData inputData) {
        List<AbstractCondition<?>> conditions = new ArrayList<>();

        if (inputData.accessible()) {
            conditions.add(
                    new Condition<Boolean>(
                            "accessible", Operator.EQ, true));
        }

        if (inputData.gender() != null) {
            conditions.add(
                    new Condition<String>(
                            "gender", Operator.EQ, inputData.gender()));
        }

        if (!inputData.washroomID().isEmpty()) {
            Optional<Washroom> washroom = washroomDAO.getById(inputData.washroomID());
            if (washroom.isEmpty()) {
                presenter.presentError("Invalid Washroom Selected.");
                return;
            } else {
                Building building = washroom.get().building();
                conditions.add(
                        new Condition<>(
                                "buildingCode", Operator.EQ, building.getBuildingCode())
                );
            }
        }

        List<Washroom> initialWashrooms = washroomDAO.getMatching(conditions);

        if (inputData.ownReviews()) {
            Optional<User> user = userDAO.getCurrentUser();
            if (user.isEmpty()) {
                presenter.presentError("Cannot filter on own reviews while the user is logged out.");
                return;
            } else {
                filterByUser(initialWashrooms, user.get());
            }
        }

        filterByRating(initialWashrooms, inputData);
        filterByCleanliness(initialWashrooms, inputData);

        presenter.present(new FilterOutputData(
                true,
                initialWashrooms,
                inputData.latitude(),
                inputData.longitude()));
    }

    /**
     * Filters washrooms, modifying the inputted list to remove washrooms which don't have
     * reviews by the given user
     * @param washrooms A Map from washroomID to Washroom object of the washrooms to be filtered.
     * @param user The user whose reviews are required for the washroom to pass the filter.
     */
    private void filterByUser(List<Washroom> washrooms, User user) {
        List<Review> reviews = reviewDAO.getReviewsByUser(user.getName());
        List<String> washroomIDs = getWashroomIDs(reviews);

        Predicate<Washroom> notHasOwnReviews = new Predicate<Washroom>() {
            @Override
            public boolean test(Washroom washroom) {
                return !washroomIDs.contains(washroom.id());
            }
        };

        washrooms.removeIf(notHasOwnReviews);
    }

    /**
     * Filters washrooms, only keeping the ones whose average rating is in the range specified
     * in the input data.
     * @param washrooms List of washrooms to filter on.
     * @param inputData The input data to use for the rating range.
     */
    private void filterByRating(List<Washroom> washrooms, FilterInputData inputData) {
        Predicate<Washroom> notInRange = new Predicate<Washroom>() {
            @Override
            public boolean test(Washroom washroom) {
                List<Review> reviews = reviewDAO.getReviewsForWashroom(washroom.id());
                List<Double> ratings = getRatings(reviews);
                Double sum = 0.0;
                for (Double rating: ratings) {
                    sum += rating;
                }
                return !(inputData.minRating() <= sum/ratings.size() &&
                        sum/ratings.size() <= inputData.maxRating());
            }
        };

        washrooms.removeIf(notInRange);
    }

    /**
     * Filters washrooms, only keeping the ones whose average cleanliness
     * is in the range specified in the input data.
     * @param washrooms List of washrooms to filter on.
     * @param inputData The input data to use for the cleanliness range.
     */
    private void filterByCleanliness(List<Washroom> washrooms, FilterInputData inputData) {
        Predicate<Washroom> notInRange = new Predicate<Washroom>() {
            @Override
            public boolean test(Washroom washroom) {
                List<Review> reviews = reviewDAO.getReviewsForWashroom(washroom.id());
                List<Double> cleans = getCleanliness(reviews);
                Double sum = 0.0;
                for (Double clean: cleans) {
                    sum += clean;
                }
                return !(inputData.minRating() <= sum/cleans.size() &&
                        sum/cleans.size() <= inputData.maxRating());
            }
        };

        washrooms.removeIf(notInRange);
    }

    private static List<String> getWashroomIDs(List<Review> reviews) {
        return reviews.stream().map(Review::washroomId).toList();
    }

    private static List<Double> getRatings(List<Review> reviews) {
        return reviews.stream().map(Review::rating).toList();
    }

    private static List<Double> getCleanliness(List<Review> reviews) {
        return reviews.stream().map(Review::cleanliness).toList();
    }


}
