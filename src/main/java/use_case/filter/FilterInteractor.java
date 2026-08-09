package use_case.filter;

import data_access.AbstractCondition;
import data_access.CollectionCondition;
import data_access.Condition;
import data_access.Operator;
import data_access.status.StatusReportDataAccessInterface;
import data_access.user.UserDataAccessInterface;
import data_access.washroom.WashroomDataAccessInterface;
import entity.Building;
import entity.StatusReport;
import entity.User;
import entity.Washroom;
import data_access.review.ReviewDataAccessInterface;

import java.time.LocalDateTime;
import java.util.*;

public class FilterInteractor implements FilterInputBoundary {
    private final Set<String> permittedWashroomNames;
    WashroomDataAccessInterface washroomDAO;
    ReviewDataAccessInterface reviewDAO;
    StatusReportDataAccessInterface statusReports;
    UserDataAccessInterface userDAO;
    FilterOutputBoundary presenter;

    public FilterInteractor(WashroomDataAccessInterface washroomDAO,
                            ReviewDataAccessInterface reviewDAO,
                            UserDataAccessInterface userDAO,
                            FilterOutputBoundary presenter) {
        this(washroomDAO, reviewDAO, null, userDAO, presenter, Set.of());
    }

    public FilterInteractor(WashroomDataAccessInterface washroomDAO,
                            ReviewDataAccessInterface reviewDAO,
                            StatusReportDataAccessInterface statusReports,
                            UserDataAccessInterface userDAO,
                            FilterOutputBoundary presenter,
                            Set<String> permittedWashroomNames) {
        this.washroomDAO = washroomDAO;
        this.reviewDAO = reviewDAO;
        this.statusReports = statusReports;
        this.userDAO = userDAO;
        this.presenter = presenter;
        this.permittedWashroomNames = Set.copyOf(permittedWashroomNames);
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

        if (!permittedWashroomNames.isEmpty()) {
            conditions.add(new CollectionCondition<>("name", Operator.IN, permittedWashroomNames));
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

        filterByCurrentStatus(initialWashrooms, inputData);

        presenter.present(new FilterOutputData(
                true,
                initialWashrooms,
                inputData.latitude(),
                inputData.longitude()));
    }

    /**
     * Filters washrooms, modifying the inputted list to remove washrooms which don't have
     * reviews by the given user
     *
     * @param washrooms A Map from washroomID to Washroom object of the washrooms to be filtered.
     * @param user      The user whose reviews are required for the washroom to pass the filter.
     */
    private void filterByUser(List<Washroom> washrooms, User user) {
        Set<String> washroomIds = reviewDAO.getReviewsByUser(user.name()).stream()
                .map(entity.Review::washroomId)
                .collect(java.util.stream.Collectors.toSet());
        washrooms.removeIf(washroom -> !washroomIds.contains(washroom.id()));
    }

    /**
     * Filters by each washroom's newest status report in the current clock hour.
     */
    private void filterByCurrentStatus(List<Washroom> washrooms, FilterInputData inputData) {
        if (statusReports == null) {
            presenter.presentError("Live status filtering is unavailable.");
            washrooms.clear();
            return;
        }
        Map<String, StatusReport> currentStatus = statusReports.getCurrentHourForWashrooms(
                washrooms.stream().map(Washroom::id).toList(), LocalDateTime.now().getHour());
        washrooms.removeIf(washroom -> {
            StatusReport status = currentStatus.get(washroom.id());
            return status == null
                    || status.busyness() > inputData.maxBusyness()
                    || status.cleanliness() < inputData.minCleanliness();
        });
    }


}
