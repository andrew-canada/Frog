package use_case.filter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
    private WashroomFilterRepository washroomDAO;
    private ReviewRepository reviewDAO;
    private StatusReportRepository statusReports;
    private CurrentUserSession session;
    private FilterOutputBoundary presenter;
    private final Set<String> permittedWashroomNames;

    public FilterInteractor(final WashroomFilterRepository washroomDAO, final ReviewRepository reviewDAO,
                            final CurrentUserSession session, final FilterOutputBoundary presenter) {
        this(washroomDAO, reviewDAO, null, session, presenter, Set.of());
    }

    public FilterInteractor(final WashroomFilterRepository washroomDAO, final ReviewRepository reviewDAO,
                            final StatusReportRepository statusReports, final CurrentUserSession session,
                            final FilterOutputBoundary presenter, final Set<String> permittedWashroomNames) {
        this.washroomDAO = washroomDAO;
        this.reviewDAO = reviewDAO;
        this.statusReports = statusReports;
        this.session = session;
        this.presenter = presenter;
        this.permittedWashroomNames = Set.copyOf(permittedWashroomNames);
    }

    @Override
    public void execute(final FilterInputData inputData) {
        String buildingCode = null;

        if (!inputData
            .washroomID()
            .isEmpty()) {
            final Optional<Washroom> washroom = washroomDAO.getById(inputData.washroomID());
            if (washroom.isEmpty()) {
                presenter.presentError("Invalid Washroom Selected.");
                return;
            }
            else {
                final Building building = washroom
                    .get()
                    .building();
                buildingCode = building.getBuildingCode();
            }
        }

        Washroom.Gender gender = null;
        if (inputData.gender() != null) {
            try {
                gender = Washroom.Gender.valueOf(inputData.gender());
            }
            catch (final IllegalArgumentException invalidGender) {
                presenter.presentError("Invalid washroom category.");
                return;
            }
        }
        final List<Washroom> initialWashrooms = washroomDAO.findMatching(
            new WashroomFilterCriteria(inputData.accessible(), gender, buildingCode, permittedWashroomNames));

        if (inputData.ownReviews()) {
            final Optional<User> user = session.currentUser();
            if (user.isEmpty()) {
                presenter.presentError("Cannot filter on own reviews while the user is logged out.");
                return;
            }
            else {
                filterByUser(initialWashrooms, user.get());
            }
        }

        if (inputData.personalPlan()) {
            final Optional<User> user = session.currentUser();
            if (user.isEmpty()) {
                presenter.presentError("Cannot filter on personal plan while the user is logged out.");
                return;
            }
            else if (user
                .map(entity.User::personalPlan)
                .orElse("")
                .equals("") || Objects.isNull(user
                .map(entity.User::personalPlan)
                .orElse(""))) {
                presenter.presentError("Please generate personal plan before filtering.");
                return;
            }
            else {
                filterByPlan(initialWashrooms, user.get());
            }
        }

        filterByCurrentStatus(initialWashrooms, inputData);

        presenter.present(new FilterOutputData(true, initialWashrooms, inputData.latitude(), inputData.longitude()));
    }

    /**
     * Filters washrooms, modifying the inputted list to remove washrooms which don't have.
     * reviews by the given user
     *
     * @param washrooms A Map from washroomID to Washroom object of the washrooms to be filtered.
     * @param user      The user whose reviews are required for the washroom to pass the filter.
     */
    private void filterByUser(final List<Washroom> washrooms, final User user) {
        final Set<String> washroomIds = reviewDAO
            .getReviewsByUser(user.name())
            .stream()
            .map(entity.Review::washroomId)
            .collect(java.util.stream.Collectors.toSet());
        washrooms.removeIf(washroom -> {
            return !washroomIds.contains(washroom.id());
        });
    }

    /**
     * Filters washrooms, modifying the inputted list to remove washrooms which are not included in the user's.
     * personal plan
     *
     * @param washrooms A Map from washroomID to Washroom object of the washrooms to be filtered.
     * @param user      The user whose personal plan is required for the washroom to pass the filter.
     */
    private void filterByPlan(final List<Washroom> washrooms, final User user) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final List<HashMap<String, String>> washroomList =
                mapper.readValue(user.personalPlan(), new TypeReference<List<HashMap<String, String>>>() {
                });
            final Set<String> washroomIds = new HashSet<>();
            for (final HashMap<String, String> washroom : washroomList) {
                washroomIds.add(washroom.get("id"));
            }
            washrooms.removeIf(washroom -> {
                return !washroomIds.contains(washroom.id());
            });
        }
        catch (final Exception entryValue) {
            presenter.presentError("Please re-generate your personal plan.");
        }

    }

    /**
     * Filters by each washroom's newest status report in the current clock hour.
     * @param inputData parameter value.
     * @param washrooms parameter value.
     */
    private void filterByCurrentStatus(final List<Washroom> washrooms, final FilterInputData inputData) {
        if (statusReports == null) {
            presenter.presentError("Live status filtering is unavailable.");
            washrooms.clear();
            return;
        }
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
