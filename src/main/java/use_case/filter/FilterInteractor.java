package use_case.filter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Building;
import entity.StatusReport;
import entity.User;
import entity.Washroom;
import use_case.port.CurrentUserSession;
import use_case.port.ReviewRepository;
import use_case.port.StatusReportRepository;

public class FilterInteractor implements FilterInputBoundary {
    private final WashroomFilterRepository washroomDao;
    private final ReviewRepository reviewDao;
    private final StatusReportRepository statusReports;
    private final CurrentUserSession session;
    private final FilterOutputBoundary presenter;
    private final Set<String> permittedWashroomNames;

    public FilterInteractor(final WashroomFilterRepository washroomDao, final ReviewRepository reviewDao,
                            final CurrentUserSession session, final FilterOutputBoundary presenter) {
        this(washroomDao, reviewDao, null, session, presenter, Set.of());
    }

    public FilterInteractor(final WashroomFilterRepository washroomDao, final ReviewRepository reviewDao,
                            final StatusReportRepository statusReports, final CurrentUserSession session,
                            final FilterOutputBoundary presenter, final Set<String> permittedWashroomNames) {
        this.washroomDao = washroomDao;
        this.reviewDao = reviewDao;
        this.statusReports = statusReports;
        this.session = session;
        this.presenter = presenter;
        this.permittedWashroomNames = Set.copyOf(permittedWashroomNames);
    }

    @Override
    public void execute(final FilterInputData inputData) {
        final String buildingCode = selectedBuildingCode(inputData.washroomID());
        final Washroom.Gender gender = parseGender(inputData.gender());
        final boolean validSelection = inputData.washroomID().isEmpty()
            || buildingCode != null;
        final boolean validGender = inputData.gender() == null || gender != null;
        if (!validSelection) {
            presenter.presentError("Invalid Washroom Selected.");
        }
        else if (!validGender) {
            presenter.presentError("Invalid washroom category.");
        }
        else {
            final List<Washroom> initialWashrooms = washroomDao.findMatching(
                new WashroomFilterCriteria(inputData.accessible(), gender, buildingCode, permittedWashroomNames));
            if (applyUserFilters(initialWashrooms, inputData)) {
                filterByCurrentStatus(initialWashrooms, inputData);
                presenter.present(new FilterOutputData(true, initialWashrooms, inputData.latitude(),
                    inputData.longitude()));
            }
        }
    }

    private String selectedBuildingCode(final String washroomId) {
        String result = null;
        if (!washroomId.isEmpty()) {
            final Optional<Washroom> washroom = washroomDao.getById(washroomId);
            if (washroom.isPresent()) {
                final Building building = washroom.get().building();
                result = building.getBuildingCode();
            }
        }
        return result;
    }

    private static Washroom.Gender parseGender(final String value) {
        Washroom.Gender result = null;
        if (value != null) {
            try {
                result = Washroom.Gender.valueOf(value);
            }
            catch (final IllegalArgumentException invalidGender) {
                result = null;
            }
        }
        return result;
    }

    private boolean applyUserFilters(final List<Washroom> washrooms, final FilterInputData inputData) {
        boolean result = true;
        final Optional<User> user = session.currentUser();
        if (inputData.ownReviews()) {
            if (user.isPresent()) {
                filterByUser(washrooms, user.get());
            }
            else {
                presenter.presentError("Cannot filter on own reviews while the user is logged out.");
                result = false;
            }
        }
        if (result && inputData.personalPlan()) {
            if (user.isEmpty()) {
                presenter.presentError("Cannot filter on personal plan while the user is logged out.");
                result = false;
            }
            else if (user.get().personalPlan() == null || user.get().personalPlan().isBlank()) {
                presenter.presentError("Please generate personal plan before filtering.");
                result = false;
            }
            else {
                result = filterByPlan(washrooms, user.get());
            }
        }
        return result;
    }

    /**
     * Filters washrooms, modifying the inputted list to remove washrooms which don't have reviews by the given user.
     *
     * @param washrooms washrooms to filter.
     * @param user user whose reviews are required.
     */
    private void filterByUser(final List<Washroom> washrooms, final User user) {
        final Set<String> washroomIds = reviewDao
            .getReviewsByUser(user.name())
            .stream()
            .map(entity.Review::washroomId)
            .collect(java.util.stream.Collectors.toSet());
        washrooms.removeIf(washroom -> !washroomIds.contains(washroom.id()));
    }

    /**
     * Filters washrooms to those included in the user's personal plan.
     *
     * @param washrooms washrooms to filter.
     * @param user user whose personal plan is required.
     * @return whether the plan was valid.
     */
    private boolean filterByPlan(final List<Washroom> washrooms, final User user) {
        boolean result = true;
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final List<Map<String, String>> washroomList = mapper.readValue(user.personalPlan(),
                new TypeReference<List<Map<String, String>>>() {
                });
            final Set<String> washroomIds = new HashSet<>();
            for (final Map<String, String> washroom : washroomList) {
                washroomIds.add(washroom.get("id"));
            }
            washrooms.removeIf(washroom -> !washroomIds.contains(washroom.id()));
        }
        catch (final JsonProcessingException entryValue) {
            presenter.presentError("Please re-generate your personal plan.");
            result = false;
        }
        return result;
    }

    /**
     * Filters by each washroom's newest status report in the current clock hour.
     *
     * @param washrooms washrooms to filter.
     * @param inputData filter thresholds.
     */
    private void filterByCurrentStatus(final List<Washroom> washrooms, final FilterInputData inputData) {
        if (statusReports == null) {
            presenter.presentError("Live status filtering is unavailable.");
            washrooms.clear();
        }
        else {
            final Map<String, StatusReport> currentStatus = statusReports.getCurrentHourForWashrooms(washrooms
                .stream()
                .map(Washroom::id)
                .toList(), LocalDateTime
                .now()
                .getHour());
            washrooms.removeIf(washroom -> {
                final StatusReport status = currentStatus.get(washroom.id());
                return status == null || status.busyness() > inputData.maxBusyness()
                    || status.cleanliness() < inputData.minCleanliness();
            });
        }
    }
}
