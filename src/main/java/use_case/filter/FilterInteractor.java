package use_case.filter;

import use_case.port.ReviewRepository;
import use_case.port.StatusReportRepository;
import use_case.port.CurrentUserSession;
import entity.Building;
import entity.StatusReport;
import entity.User;
import entity.Washroom;

import java.time.LocalDateTime;
import java.util.*;

public class FilterInteractor implements FilterInputBoundary {
    private final Set<String> permittedWashroomNames;
    WashroomFilterRepository washroomDAO;
    ReviewRepository reviewDAO;
    StatusReportRepository statusReports;
    CurrentUserSession session;
    FilterOutputBoundary presenter;

    public FilterInteractor(WashroomFilterRepository washroomDAO,
                            ReviewRepository reviewDAO,
                            CurrentUserSession session,
                            FilterOutputBoundary presenter) {
        this(washroomDAO, reviewDAO, null, session, presenter, Set.of());
    }

    public FilterInteractor(WashroomFilterRepository washroomDAO,
                            ReviewRepository reviewDAO,
                            StatusReportRepository statusReports,
                            CurrentUserSession session,
                            FilterOutputBoundary presenter,
                            Set<String> permittedWashroomNames) {
        this.washroomDAO = washroomDAO;
        this.reviewDAO = reviewDAO;
        this.statusReports = statusReports;
        this.session = session;
        this.presenter = presenter;
        this.permittedWashroomNames = Set.copyOf(permittedWashroomNames);
    }

    @Override
    public void execute(FilterInputData inputData) {
        String buildingCode = null;

        if (!inputData.washroomID().isEmpty()) {
            Optional<Washroom> washroom = washroomDAO.getById(inputData.washroomID());
            if (washroom.isEmpty()) {
                presenter.presentError("Invalid Washroom Selected.");
                return;
            } else {
                Building building = washroom.get().building();
                buildingCode = building.getBuildingCode();
            }
        }

        Washroom.Gender gender = null;
        if (inputData.gender() != null) {
            try {
                gender = Washroom.Gender.valueOf(inputData.gender());
            } catch (IllegalArgumentException invalidGender) {
                presenter.presentError("Invalid washroom category.");
                return;
            }
        }
        List<Washroom> initialWashrooms = washroomDAO.findMatching(new WashroomFilterCriteria(
                inputData.accessible(), gender, buildingCode, permittedWashroomNames));

        if (inputData.ownReviews()) {
            Optional<User> user = session.currentUser();
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
