import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import entity.Building;
import entity.Report;
import entity.Review;
import entity.ReviewSummary;
import entity.Route;
import entity.User;
import entity.Washroom;
import use_case.account.load_account.LoadAccountInteractor;
import use_case.account.load_account.LoadAccountOutputData;
import use_case.account.personal_plan.CalendarContentReader;
import use_case.account.personal_plan.PersonalPlanGenerator;
import use_case.account.personal_plan.PersonalPlanInputData;
import use_case.account.personal_plan.PersonalPlanInteractor;
import use_case.account.change_password.ChangePasswordInteractor;
import use_case.account.change_username.ChangeUsernameInteractor;
import use_case.account.delete_account.DeleteAccountInteractor;
import use_case.directions.GetDirectionsInputData;
import use_case.directions.GetDirectionsInteractor;
import use_case.directions.GetDirectionsOutputData;
import use_case.filter.FilterInputData;
import use_case.filter.FilterInteractor;
import use_case.filter.FilterOutputData;
import use_case.filter.WashroomFilterRepository;
import use_case.login.Passwords;
import use_case.logout.LogoutInteractor;
import use_case.moderate_reviews.ReportedReviewsDataAccessInterface;
import use_case.port.CurrentUserSession;
import use_case.port.ReviewRepository;
import use_case.port.RouteGateway;
import use_case.port.StatusReportRepository;
import use_case.port.UserRepository;
import use_case.port.WashroomRepository;
import use_case.report_review.ReviewReportDataAccessInterface;
import use_case.sort_review.ReviewSortOrder;
import use_case.sort_review.SortReviewInputData;
import use_case.sort_review.SortReviewInteractor;
import use_case.sort_washrooms.SortWashroomInputData;
import use_case.sort_washrooms.SortWashroomInteractor;
import use_case.sort_washrooms.SortWashroomsOutputData;
import use_case.sort_washrooms.WashroomSortOrder;
import use_case.status_report.SubmitStatusReportInputData;
import use_case.status_report.SubmitStatusReportInteractor;
import use_case.status_report.SubmitStatusReportOutputData;
import use_case.signup.SignupInputData;
import use_case.signup.SignupInteractor;
import use_case.signup.SignupOutputData;
import use_case.view_reviews.ViewReviewsOutputBoundary;
import use_case.view_reviews.ViewReviewsOutputData;
import use_case.view_reviews.ViewReviewsInteractor;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;
import use_case.write_review.WriteReviewInputData;
import use_case.write_review.WriteReviewInteractor;
import use_case.write_review.WriteReviewOutputData;

/** Exercises core edge cases that are independent of infrastructure and Swing. */
class CoreBehaviorCoverageTest {
    private static final Building BUILDING = new Building("BA", "Bahen", 43.66, -79.39);
    private static final Washroom WASHROOM = new Washroom("w1", "Main washroom | West wing washrooms", BUILDING, "2",
        true, Washroom.Gender.ALL_GENDER, 3, 2, "near elevators", new ReviewSummary(4, 3, 2));

    @Test
    void passwordHashingHandlesValidAndMalformedValues() {
        final String encoded = Passwords.hash("secret");
        assertTrue(Passwords.matches("secret", encoded));
        assertFalse(Passwords.matches("wrong", encoded));
        assertFalse(Passwords.matches(null, encoded));
        assertFalse(Passwords.matches("secret", null));
        assertFalse(Passwords.matches("secret", "bad"));
        assertFalse(Passwords.matches("secret", "x:salt:hash"));
        assertFalse(Passwords.matches("secret", "1:not-base64:also-not-base64"));
        assertFalse(Passwords.matches("secret", "not-a-number:c2FsdA==:aGFzaA=="));
        assertThrowsIllegalArgument(() -> Passwords.hash(null));
        assertThrowsIllegalArgument(() -> Passwords.hash(""));
    }

    @Test
    void writeReviewValidatesAndStoresNormalizedValues() {
        final List<Review> saved = new ArrayList<>();
        final AtomicReference<WriteReviewOutputData> output = new AtomicReference<>();
        final ReviewRepository repository = new ReviewRepository() {
            @Override public List<Review> getReviewsForWashroom(final String id) { return List.of(); }
            @Override public ReviewSummary getSummary(final String id) { return ReviewSummary.empty(); }
            @Override public List<Review> getReviewsByUser(final String username) { return List.of(); }
            @Override public void save(final Review review) { saved.add(review); }
        };
        final WriteReviewInteractor interactor = new WriteReviewInteractor(repository, output::set);
        interactor.execute(new WriteReviewInputData("", "alice", 4, 4, "comment"));
        assertEquals("Choose a washroom before writing a review.", output.get().message());
        interactor.execute(new WriteReviewInputData("w1", "alice", 0, 4, "comment"));
        assertEquals("Ratings must be between 1 and 5.", output.get().message());
        interactor.execute(new WriteReviewInputData("w1", "alice", 4, 6, "comment"));
        assertEquals("Ratings must be between 1 and 5.", output.get().message());
        interactor.execute(new WriteReviewInputData("w1", "alice", 4, 4, "   "));
        assertEquals("Write a short review before submitting.", output.get().message());
        interactor.execute(new WriteReviewInputData("w1", null, 4, 4, "  useful  "));
        assertTrue(output.get().success());
        assertEquals(1, saved.size());
        assertEquals("Anonymous", saved.getFirst().authorUsername());
        assertEquals("useful", saved.getFirst().comment());
        assertEquals(4, saved.getFirst().rating());
        interactor.execute(new WriteReviewInputData("w1", " alice ", 5, 5, "  useful  "));
        assertEquals(" alice ", saved.getLast().authorUsername());
        assertEquals("useful", saved.getLast().comment());
    }

    @Test
    void loginRejectsMissingUsersAndModerationUsesOtherReasonFallback() {
        final database.user.InMemoryUserDataAccessObject users = new database.user.InMemoryUserDataAccessObject();
        final AtomicReference<use_case.login.LoginOutputData> loginOutput = new AtomicReference<>();
        final CurrentUserSession session = new CurrentUserSession() {
            @Override public Optional<User> currentUser() { return Optional.empty(); }
            @Override public void setCurrentUser(final User user) { }
            @Override public void clear() { }
        };
        final use_case.port.PasswordHasher passwords = new use_case.port.PasswordHasher() {
            @Override public String hash(final String password) { return "hash"; }
            @Override public boolean matches(final String password, final String storedHash) { return false; }
            @Override public boolean isCurrentHash(final String storedHash) { return true; }
        };
        new use_case.login.LoginInteractor(users, session, passwords, loginOutput::set).execute(
            new use_case.login.LoginInputData("missing", "bad"));
        assertFalse(loginOutput.get().success());

        final Review review = new Review("moderated", "w1", "alice", 4, 4, "comment", 0, LocalDate.now());
        final database.review.InMemoryReviewDataAccessObject reviewStore =
            new database.review.InMemoryReviewDataAccessObject(List.of(review));
        reviewStore.save(new Report("report", "moderated", "bob", List.of(), "needs review", LocalDateTime.now()));
        final database.user.InMemoryUserDataAccessObject moderators =
            new database.user.InMemoryUserDataAccessObject();
        moderators.save(new User("mod", "hash", "", true));
        final AtomicReference<use_case.moderate_reviews.ModerateReviewsOutputData> moderationOutput =
            new AtomicReference<>();
        final use_case.moderate_reviews.ModerateReviewsInteractor moderation =
            new use_case.moderate_reviews.ModerateReviewsInteractor(reviewStore, reviewStore,
                new SingleWashroomRepository(WASHROOM), moderators, moderationOutput::set);
        moderation.loadReportedReviews();
        assertEquals("Other", moderationOutput.get().reportedReviews().getFirst().additionalDetails().getFirst().reason());
    }

    @Test
    void accountLoadingAndLogoutHandleBothSessionStates() {
        final AtomicReference<LoadAccountOutputData> loaded = new AtomicReference<>();
        final AtomicReference<Boolean> cleared = new AtomicReference<>(false);
        final User user = new User("alice", "hash", "plan");
        final CurrentUserSession session = new CurrentUserSession() {
            User current;
            @Override public Optional<User> currentUser() { return Optional.ofNullable(current); }
            @Override public void setCurrentUser(final User value) { current = value; }
            @Override public void clear() { current = null; cleared.set(true); }
        };
        new LoadAccountInteractor(session, loaded::set).execute();
        assertEquals(new LoadAccountOutputData("", ""), loaded.get());
        session.setCurrentUser(user);
        new LoadAccountInteractor(session, loaded::set).execute();
        assertEquals(new LoadAccountOutputData("alice", "plan"), loaded.get());
        final AtomicReference<Boolean> presented = new AtomicReference<>(false);
        new LogoutInteractor(session, () -> presented.set(true)).execute();
        assertTrue(cleared.get() && presented.get());
        assertTrue(session.currentUser().isEmpty());
    }

    @Test
    void enumLabelsCoverEverySupportedAndFallbackValue() {
        assertEquals(ReviewSortOrder.MOST_HELPFUL, ReviewSortOrder.fromDisplayLabel("Most Helpful"));
        assertEquals(ReviewSortOrder.HIGHEST_RATED, ReviewSortOrder.fromDisplayLabel("Highest Rated"));
        assertEquals(ReviewSortOrder.LOWEST_RATED, ReviewSortOrder.fromDisplayLabel("Lowest Rated"));
        assertEquals(ReviewSortOrder.NEWEST, ReviewSortOrder.fromDisplayLabel("Newest"));
        assertEquals(ReviewSortOrder.VOTED_BY_ME, ReviewSortOrder.fromDisplayLabel("Voted by Me"));
        assertEquals(ReviewSortOrder.RELEVANCE, ReviewSortOrder.fromDisplayLabel("Relevant"));
        assertEquals(ReviewSortOrder.RELEVANCE, ReviewSortOrder.fromDisplayLabel(null));
        assertEquals(ReviewSortOrder.RELEVANCE, ReviewSortOrder.fromDisplayLabel("other"));
        assertEquals(WashroomSortOrder.HIGHEST_RATED, WashroomSortOrder.fromDisplayLabel("Highest Rated"));
        assertEquals(WashroomSortOrder.NEAREST, WashroomSortOrder.fromDisplayLabel("Nearest"));
        assertEquals(WashroomSortOrder.ALPHABETICAL, WashroomSortOrder.fromDisplayLabel("Alphabetical"));
        assertEquals(WashroomSortOrder.ALPHABETICAL, WashroomSortOrder.fromDisplayLabel(null));
        assertEquals(WashroomSortOrder.ALPHABETICAL, WashroomSortOrder.fromDisplayLabel("other"));
    }

    @Test
    void directionsReportsUnavailableWithAndWithoutMessages() {
        final AtomicReference<GetDirectionsOutputData> output = new AtomicReference<>();
        final WashroomRepository washrooms = new SingleWashroomRepository(WASHROOM);
        final RouteGateway failingWithMessage = (origin, destination) -> {
            throw new IllegalStateException("provider down");
        };
        final GetDirectionsInteractor first = new GetDirectionsInteractor(washrooms, failingWithMessage, output::set);
        first.execute(new GetDirectionsInputData(1, 2, "w1"));
        assertEquals("provider down", output.get().message());
        final GetDirectionsInteractor second = new GetDirectionsInteractor(washrooms, (origin, destination) -> {
            throw new IllegalStateException();
        }, output::set);
        second.execute(new GetDirectionsInputData(1, 2, "w1"));
        assertEquals("Directions are temporarily unavailable", output.get().message());
        final GetDirectionsInteractor missing = new GetDirectionsInteractor(new SingleWashroomRepository((Washroom) null),
            (origin, destination) -> new Route(List.of(), 1, 1), output::set);
        missing.execute(new GetDirectionsInputData(1, 2, "missing"));
        assertFalse(output.get().success());
    }

    @Test
    void sortWashroomsHandlesNearestAndDefaultOrder() {
        final Washroom far = new Washroom("far", "Zeta", new Building("BA", "Bahen", 44, -79), "1", true,
            Washroom.Gender.ALL_GENDER, 1, 1, "inside", new ReviewSummary(5, 5, 1));
        final Washroom near = new Washroom("near", "Alpha", BUILDING, "1", true, Washroom.Gender.ALL_GENDER, 1, 1,
            "inside", new ReviewSummary(1, 1, 1));
        final AtomicReference<SortWashroomsOutputData> output = new AtomicReference<>();
        final WashroomRepository repository = new SingleWashroomRepository(List.of(far, near));
        final SortWashroomInteractor interactor = new SortWashroomInteractor(repository, output::set);
        interactor.execute(new SortWashroomInputData(WashroomSortOrder.NEAREST, List.of("far", "near"), 43.66,
            -79.39));
        assertEquals("near", output.get().washrooms().getFirst().id());
        interactor.execute(new SortWashroomInputData(null, List.of("far", "near"), 0, 0));
        assertEquals("near", output.get().washrooms().getFirst().id());
    }

    @Test
    void statusRepositoryAndWashroomRepositoryDefaultMethodsWork() {
        final LocalDateTime old = LocalDateTime.of(2026, 1, 1, 8, 0);
        final LocalDateTime newer = old.plusMinutes(10);
        final entity.StatusReport oldReport = new entity.StatusReport("w1", "a", 2, 3, entity.MaintenanceIssue.NONE,
            old);
        final entity.StatusReport newReport = new entity.StatusReport("w1", "a", 4, 5, entity.MaintenanceIssue.NONE,
            newer);
        final StatusReportRepository reports = new StatusReportRepository() {
            @Override public void save(final entity.StatusReport report) { }
            @Override public List<entity.StatusReport> getRecentForWashroom(final String id, final LocalDateTime since) {
                return List.of(oldReport, newReport);
            }
            @Override public List<entity.StatusReport> getForWashroom(final String id, final LocalDateTime from,
                                                                        final LocalDateTime to) { return List.of(); }
        };
        assertEquals(newReport, reports.getCurrentHourForWashrooms(List.of("w1", "missing"), 8).get("w1"));
        final WashroomRepository washrooms = new SingleWashroomRepository(List.of(WASHROOM));
        assertEquals(List.of(WASHROOM), washrooms.getByIds(List.of("w1", "missing")));
    }

    @Test
    void busynessAndStatusUseCasesReportEmptyAndInvalidStates() {
        final AtomicReference<use_case.busyness.BusynessStatsOutputData> busynessOutput = new AtomicReference<>();
        final use_case.busyness.BusynessStatsInteractor busyness = new use_case.busyness.BusynessStatsInteractor(
            new StatusReportRepository() {
                @Override public void save(final entity.StatusReport report) { }
                @Override public List<entity.StatusReport> getRecentForWashroom(final String id, final LocalDateTime since) { return List.of(); }
                @Override public List<entity.StatusReport> getForWashroom(final String id, final LocalDateTime from, final LocalDateTime to) { return List.of(); }
            },
            (building, day) -> List.of(), busynessOutput::set);
        busyness.execute(new use_case.busyness.BusynessStatsInputData("w1", "BA", java.time.DayOfWeek.MONDAY));
        assertEquals("No status or enrollment data is stored yet", busynessOutput.get().sourceNote());
        assertEquals("no data", busynessOutput.get().buckets().getFirst().dominantSource());

        final AtomicReference<SubmitStatusReportOutputData> statusOutput = new AtomicReference<>();
        final StatusReportRepository reports = new StatusReportRepository() {
            @Override public void save(final entity.StatusReport report) { }
            @Override public List<entity.StatusReport> getRecentForWashroom(final String id, final LocalDateTime since) { return List.of(); }
            @Override public List<entity.StatusReport> getForWashroom(final String id, final LocalDateTime from, final LocalDateTime to) { return List.of(); }
        };
        final SubmitStatusReportInteractor status = new SubmitStatusReportInteractor(reports, statusOutput::set);
        status.execute(new SubmitStatusReportInputData("w1", 0, 3, entity.MaintenanceIssue.NONE, "alice"));
        assertEquals("Choose values from 1 to 5", statusOutput.get().message());
        status.execute(new SubmitStatusReportInputData("w1", 3, 6, entity.MaintenanceIssue.NONE, "alice"));
        assertFalse(statusOutput.get().success());
    }

    @Test
    void signupHandlesNullInvalidDuplicateAndTrimmedNames() {
        final class Users implements UserRepository, CurrentUserSession {
            final Set<String> names = new java.util.HashSet<>();
            User current;
            @Override public Optional<User> get(final String name) { return Optional.ofNullable(current); }
            @Override public boolean existsByName(final String name) { return names.contains(name); }
            @Override public void save(final User user) { names.add(user.username()); current = user; }
            @Override public void removeUser(final String name) { names.remove(name); }
            @Override public Optional<User> currentUser() { return Optional.ofNullable(current); }
            @Override public void setCurrentUser(final User user) { current = user; }
            @Override public void clear() { current = null; }
        }
        final Users users = new Users();
        final AtomicReference<SignupOutputData> output = new AtomicReference<>();
        final use_case.port.PasswordHasher hasher = new use_case.port.PasswordHasher() {
            @Override public String hash(final String password) { return "hash:" + password; }
            @Override public boolean matches(final String password, final String storedHash) { return true; }
            @Override public boolean isCurrentHash(final String storedHash) { return true; }
        };
        final SignupInteractor interactor = new SignupInteractor(users, users, hasher, output::set);
        interactor.execute(new SignupInputData(null, "1234"));
        assertFalse(output.get().success());
        interactor.execute(new SignupInputData("ab", null));
        assertFalse(output.get().success());
        interactor.execute(new SignupInputData(" alice ", "1234"));
        assertTrue(output.get().success());
        interactor.execute(new SignupInputData("alice", "1234"));
        assertEquals("That username is already taken", output.get().message());
    }

    @Test
    void valueObjectsRejectWrongArity() {
        assertThrowsIllegalArgument(() -> new FilterInputData(1));
        assertThrowsIllegalArgument(() -> new ViewReviewsOutputData("w1"));
        assertThrowsIllegalArgument(() -> new interface_adapter.account.AccountState("u"));
        assertThrowsIllegalArgument(() -> new interface_adapter.view_reviews.ReviewsViewModel.State("w1"));
    }

    @Test
    void accountUseCasesRejectMissingSessions() {
        final database.user.InMemoryUserDataAccessObject users = new database.user.InMemoryUserDataAccessObject();
        final AtomicReference<Object> output = new AtomicReference<>();
        new ChangeUsernameInteractor(users, users, data -> output.set(data)).execute(
            new use_case.account.change_username.ChangeUsernameInputData("new"));
        assertEquals("Not logged in", ((use_case.account.change_username.ChangeUsernameOutputData) output.get()).message());
        final use_case.port.PasswordHasher hasher = new use_case.port.PasswordHasher() {
            @Override public String hash(final String password) { return "hash"; }
            @Override public boolean matches(final String password, final String storedHash) { return true; }
            @Override public boolean isCurrentHash(final String storedHash) { return true; }
        };
        new ChangePasswordInteractor(users, users, hasher, data -> output.set(data)).execute(
            new use_case.account.change_password.ChangePasswordInputData("new", "new"));
        assertEquals("Not logged in", ((use_case.account.change_password.ChangePasswordOutputData) output.get()).message());
        new DeleteAccountInteractor(users, users, data -> output.set(data)).execute();
        assertEquals("You are not logged in", ((use_case.account.delete_account.DeleteAccountOutputData) output.get()).message());
    }

    @Test
    void viewReviewsHandlesMissingAndPlainNames() {
        final AtomicReference<String> error = new AtomicReference<>();
        final HelpfulVoteDataAccessInterface votes = new HelpfulVoteDataAccessInterface() {
            @Override public boolean hasVoted(final String id, final String user) { return false; }
            @Override public Set<String> votedReviewIds(final Collection<String> ids, final String user) { return Set.of(); }
            @Override public void addVote(final String id, final String user) { }
            @Override public void removeVote(final String id, final String user) { }
        };
        final ReviewReportDataAccessInterface reports = new ReviewReportDataAccessInterface() {
            @Override public void save(final Report report) { }
            @Override public boolean hasReported(final String id, final String user) { return false; }
            @Override public Set<String> reportedReviewIds(final Collection<String> ids, final String user) { return Set.of(); }
        };
        final ViewReviewsInteractor missing = new ViewReviewsInteractor(new EmptyReviews(),
            new SingleWashroomRepository((Washroom) null), votes, reports, new ViewReviewsOutputBoundary() {
                @Override public void present(final ViewReviewsOutputData data) { }
                @Override public void presentError(final String message) { error.set(message); }
            });
        missing.execute(new use_case.view_reviews.ViewReviewsInputData("missing", "alice"));
        assertEquals("Washroom not found", error.get());
        final Washroom plain = new Washroom("plain", "Plain", BUILDING, "1", true, Washroom.Gender.ALL_GENDER, 1, 1,
            "inside", ReviewSummary.empty());
        final AtomicReference<ViewReviewsOutputData> output = new AtomicReference<>();
        new ViewReviewsInteractor(new EmptyReviews(), new SingleWashroomRepository(plain), votes, reports,
            new ViewReviewsOutputBoundary() {
                @Override public void present(final ViewReviewsOutputData data) { output.set(data); }
                @Override public void presentError(final String message) { throw new AssertionError(message); }
        }).execute(new use_case.view_reviews.ViewReviewsInputData("plain", "alice"));
        assertEquals("Plain", output.get().subtitle());
        new ViewReviewsInteractor(new EmptyReviews(), new SingleWashroomRepository(WASHROOM), votes, reports,
            new ViewReviewsOutputBoundary() {
                @Override public void present(final ViewReviewsOutputData data) { output.set(data); }
                @Override public void presentError(final String message) { throw new AssertionError(message); }
            }).execute(new use_case.view_reviews.ViewReviewsInputData("w1", "alice"));
        assertEquals("West wing", output.get().subtitle());
    }

    @Test
    void sortingReviewsCoversAllComparatorsAndMissingWashroom() {
        final Review helpful = new Review("helpful", "w1", "bob", 2, 2, "h", 10, LocalDate.now());
        final Review high = new Review("high", "w1", "alice", 5, 5, "high", 1, LocalDate.now());
        final ReviewRepository reviews = new ReviewRepository() {
            @Override public List<Review> getReviewsForWashroom(final String id) { return List.of(helpful, high); }
            @Override public ReviewSummary getSummary(final String id) { return ReviewSummary.fromReviews(List.of(helpful, high)); }
            @Override public List<Review> getReviewsByUser(final String username) { return List.of(); }
            @Override public void save(final Review review) { }
        };
        final CurrentUserSession session = new CurrentUserSession() {
            @Override public Optional<User> currentUser() { return Optional.of(new User("alice", "hash", "")); }
            @Override public void setCurrentUser(final User user) { }
            @Override public void clear() { }
        };
        final HelpfulVoteDataAccessInterface votes = new HelpfulVoteDataAccessInterface() {
            @Override public boolean hasVoted(final String id, final String user) { return false; }
            @Override public Set<String> votedReviewIds(final Collection<String> ids, final String user) { return Set.of("high"); }
            @Override public void addVote(final String id, final String user) { }
            @Override public void removeVote(final String id, final String user) { }
        };
        final ReviewReportDataAccessInterface reports = new ReviewReportDataAccessInterface() {
            @Override public void save(final Report report) { }
            @Override public boolean hasReported(final String id, final String user) { return false; }
            @Override public Set<String> reportedReviewIds(final Collection<String> ids, final String user) { return Set.of(); }
        };
        final AtomicReference<ViewReviewsOutputData> output = new AtomicReference<>();
        final ViewReviewsOutputBoundary presenter = new ViewReviewsOutputBoundary() {
            @Override public void present(final ViewReviewsOutputData data) { output.set(data); }
            @Override public void presentError(final String message) { throw new AssertionError(message); }
        };
        final SortReviewInteractor interactor = new SortReviewInteractor(reviews, session,
            new SingleWashroomRepository(WASHROOM), votes, reports, presenter);
        for (final ReviewSortOrder order : ReviewSortOrder.values()) {
            interactor.execute(new SortReviewInputData(order, "w1"));
            assertNotNull(output.get());
        }
        interactor.execute(new SortReviewInputData(null, "w1"));
        assertNotNull(output.get());
        final AtomicReference<String> error = new AtomicReference<>();
        final SortReviewInteractor missing = new SortReviewInteractor(reviews, session,
            new SingleWashroomRepository((Washroom) null), votes, reports, new ViewReviewsOutputBoundary() {
                @Override public void present(final ViewReviewsOutputData data) { }
                @Override public void presentError(final String message) { error.set(message); }
            });
        missing.execute(new SortReviewInputData(ReviewSortOrder.RELEVANCE, "missing"));
        assertEquals("Washroom not found", error.get());
        final CurrentUserSession guest = new CurrentUserSession() {
            @Override public Optional<User> currentUser() { return Optional.empty(); }
            @Override public void setCurrentUser(final User user) { }
            @Override public void clear() { }
        };
        new SortReviewInteractor(reviews, guest, new SingleWashroomRepository(WASHROOM), votes, reports, presenter)
            .execute(new SortReviewInputData(ReviewSortOrder.VOTED_BY_ME, "w1"));
    }

    @Test
    void filterRejectsInvalidSelectionsAndUnavailableStatus() {
        final AtomicReference<String> error = new AtomicReference<>();
        final AtomicReference<FilterOutputData> output = new AtomicReference<>();
        final WashroomFilterRepository washrooms = new SingleFilterRepository(WASHROOM);
        final UserRepository reviews = new UserRepository() {
            @Override public Optional<User> get(final String username) { return Optional.empty(); }
            @Override public boolean existsByName(final String username) { return false; }
            @Override public void save(final User user) { }
            @Override public void removeUser(final String username) { }
        };
        final CurrentUserSession loggedOut = new CurrentUserSession() {
            @Override public Optional<User> currentUser() { return Optional.empty(); }
            @Override public void setCurrentUser(final User user) { }
            @Override public void clear() { }
        };
        final FilterInteractor interactor = new FilterInteractor(washrooms, new EmptyReviews(), loggedOut,
            new use_case.filter.FilterOutputBoundary() {
                @Override public void present(final FilterOutputData data) { output.set(data); }
                @Override public void presentError(final String message) { error.set(message); }
            });
        interactor.execute(new FilterInputData(1, 1, false, null, "missing", false, false, 0, 0));
        assertEquals("Invalid Washroom Selected.", error.get());
        interactor.execute(new FilterInputData(1, 1, false, "INVALID", "", false, false, 0, 0));
        assertEquals("Invalid washroom category.", error.get());
        interactor.execute(new FilterInputData(1, 1, false, null, "", false, false, 0, 0));
        assertEquals("Live status filtering is unavailable.", error.get());
        assertNotNull(output.get());
        interactor.execute(new FilterInputData(1, 1, false, null, "", true, false, 0, 0));
        assertEquals("Cannot filter on own reviews while the user is logged out.", error.get());
        interactor.execute(new FilterInputData(1, 1, false, null, "", false, true, 0, 0));
        assertEquals("Cannot filter on personal plan while the user is logged out.", error.get());
    }

    @Test
    void filterAppliesOwnReviewsPlansAndCurrentStatus() {
        final AtomicReference<String> error = new AtomicReference<>();
        final AtomicReference<FilterOutputData> output = new AtomicReference<>();
        final User current = new User("alice", "hash", "");
        final CurrentUserSession session = new CurrentUserSession() {
            @Override public Optional<User> currentUser() { return Optional.of(current); }
            @Override public void setCurrentUser(final User user) { }
            @Override public void clear() { }
        };
        final ReviewRepository reviews = new ReviewRepository() {
            @Override public List<Review> getReviewsForWashroom(final String id) { return List.of(); }
            @Override public ReviewSummary getSummary(final String id) { return ReviewSummary.empty(); }
            @Override public List<Review> getReviewsByUser(final String username) {
                return List.of(new Review("r", "w1", username, 4, 4, "", 0, LocalDate.now()));
            }
            @Override public void save(final Review review) { }
        };
        final StatusReportRepository statuses = new StatusReportRepository() {
            @Override public void save(final entity.StatusReport report) { }
            @Override public List<entity.StatusReport> getRecentForWashroom(final String id, final LocalDateTime since) {
                return List.of();
            }
            @Override public List<entity.StatusReport> getForWashroom(final String id, final LocalDateTime from,
                                                                        final LocalDateTime to) { return List.of(); }
            @Override public java.util.Map<String, entity.StatusReport> getCurrentHourForWashrooms(
                    final List<String> ids, final int hour) {
                return java.util.Map.of("w1", new entity.StatusReport("w1", "alice", 2, 4,
                    entity.MaintenanceIssue.NONE, LocalDateTime.now()));
            }
        };
        final use_case.filter.FilterOutputBoundary presenter = new use_case.filter.FilterOutputBoundary() {
            @Override public void present(final FilterOutputData data) { output.set(data); }
            @Override public void presentError(final String message) { error.set(message); }
        };
        final FilterInteractor interactor = new FilterInteractor(new SingleFilterRepository(WASHROOM), reviews, statuses,
            session, presenter, Set.of());
        interactor.execute(new FilterInputData(5, 1, false, null, "", true, false, 43, -79));
        assertEquals(1, output.get().washrooms().size());
        interactor.execute(new FilterInputData(5, 1, false, "ALL_GENDER", "", false, false, 43, -79));
        assertEquals(1, output.get().washrooms().size());
        interactor.execute(new FilterInputData(5, 1, false, null, "", false, true, 43, -79));
        assertEquals("Please generate personal plan before filtering.", error.get());

        final User planned = new User("alice", "hash",
            "[{\"id\":\"other\"}]", false);
        final CurrentUserSession plannedSession = new CurrentUserSession() {
            @Override public Optional<User> currentUser() { return Optional.of(planned); }
            @Override public void setCurrentUser(final User user) { }
            @Override public void clear() { }
        };
        final FilterInteractor plannedFilter = new FilterInteractor(new SingleFilterRepository(WASHROOM), reviews,
            statuses, plannedSession, presenter, Set.of());
        plannedFilter.execute(new FilterInputData(5, 1, false, null, "", false, true, 43, -79));
        assertTrue(output.get().washrooms().isEmpty());
        final User malformed = new User("alice", "hash", "not json", false);
        final CurrentUserSession malformedSession = new CurrentUserSession() {
            @Override public Optional<User> currentUser() { return Optional.of(malformed); }
            @Override public void setCurrentUser(final User user) { }
            @Override public void clear() { }
        };
        new FilterInteractor(new SingleFilterRepository(WASHROOM), reviews, statuses, malformedSession, presenter,
            Set.of()).execute(new FilterInputData(5, 1, false, null, "", false, true, 43, -79));
        assertEquals("Please re-generate your personal plan.", error.get());

        final StatusReportRepository noStatusForId = new StatusReportRepository() {
            @Override public void save(final entity.StatusReport report) { }
            @Override public List<entity.StatusReport> getRecentForWashroom(final String id, final LocalDateTime since) { return List.of(); }
            @Override public List<entity.StatusReport> getForWashroom(final String id, final LocalDateTime from, final LocalDateTime to) { return List.of(); }
            @Override public java.util.Map<String, entity.StatusReport> getCurrentHourForWashrooms(final List<String> ids, final int hour) { return java.util.Map.of(); }
        };
        new FilterInteractor(new SingleFilterRepository(WASHROOM), reviews, noStatusForId, session, presenter, Set.of())
            .execute(new FilterInputData(5, 1, false, null, "", false, false, 43, -79));
        assertTrue(output.get().washrooms().isEmpty());
    }

    @Test
    void personalPlanRejectsInvalidInputsAndGenerationFailures() {
        final AtomicReference<use_case.account.personal_plan.PersonalPlanOutputData> output = new AtomicReference<>();
        final User user = new User("alice", "hash", "", false);
        final class Session implements CurrentUserSession {
            User current = user;
            @Override public Optional<User> currentUser() { return Optional.ofNullable(current); }
            @Override public void setCurrentUser(final User value) { current = value; }
            @Override public void clear() { current = null; }
        }
        final Session session = new Session();
        final UserRepository users = new UserRepository() {
            @Override public Optional<User> get(final String name) { return Optional.ofNullable(session.current); }
            @Override public boolean existsByName(final String name) { return true; }
            @Override public void save(final User value) { session.current = value; }
            @Override public void removeUser(final String name) { }
        };
        final WashroomRepository washrooms = new SingleWashroomRepository(WASHROOM);
        final CalendarContentReader calendar = path -> "calendar";
        final PersonalPlanGenerator valid = (calendarText, trips, semester, available) ->
            "[{\"Day of week\":\"Mon\",\"Time (nearest hour) of washroom break\":\"10:00\",\"Washroom id\":\"w1\"}]";
        final PersonalPlanInteractor interactor = new PersonalPlanInteractor(users, session, washrooms, calendar,
            valid, output::set);
        session.current = null;
        interactor.execute(new PersonalPlanInputData("schedule.ics", "1", "Fall"));
        assertEquals("You need an account", output.get().message());
        session.current = user;
        interactor.execute(new PersonalPlanInputData("schedule.txt", "1", "Fall"));
        assertEquals("Please upload a .ics file", output.get().message());
        interactor.execute(new PersonalPlanInputData("schedule.ics", "0", "Fall"));
        assertEquals("Please input a positive whole number of trips", output.get().message());
        interactor.execute(new PersonalPlanInputData("schedule.ics", "abc", "Fall"));
        assertEquals("Please input a positive whole number of trips", output.get().message());
        final PersonalPlanInteractor ioFailure = new PersonalPlanInteractor(users, session, washrooms,
            path -> { throw new IOException("read failed"); }, valid, output::set);
        ioFailure.execute(new PersonalPlanInputData("schedule.ics", "1", "Fall"));
        assertFalse(output.get().success());
        final PersonalPlanInteractor emptyPlan = new PersonalPlanInteractor(users, session, washrooms, calendar,
            (calendarText, trips, semester, available) -> "[]", output::set);
        emptyPlan.execute(new PersonalPlanInputData("schedule.ics", "1", "Fall"));
        assertFalse(output.get().success());
        final PersonalPlanInteractor unknownWashroom = new PersonalPlanInteractor(users, session, washrooms, calendar,
            (calendarText, trips, semester, available) -> "[{\"Day of week\":\"Mon\",\"Time (nearest hour) of washroom break\":\"10:00\",\"Washroom id\":\"missing\"}]",
            output::set);
        unknownWashroom.execute(new PersonalPlanInputData("schedule.ics", "1", "Fall"));
        assertFalse(output.get().success());
        final PersonalPlanInteractor missingTime = new PersonalPlanInteractor(users, session, washrooms, calendar,
            (calendarText, trips, semester, available) -> "[{\"Day of week\":\"Mon\",\"Washroom id\":\"w1\"}]",
            output::set);
        missingTime.execute(new PersonalPlanInputData("schedule.ics", "1", "Fall"));
        assertFalse(output.get().success());
        interactor.execute(new PersonalPlanInputData("schedule.ics", "1", "Fall"));
        assertTrue(output.get().success());
    }

    private static void assertThrowsIllegalArgument(final Runnable action) {
        try {
            action.run();
        }
        catch (final IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("expected IllegalArgumentException");
    }

    private record SingleWashroomRepository(List<Washroom> values) implements WashroomRepository {
        SingleWashroomRepository(final Washroom value) { this(value == null ? List.of() : List.of(value)); }
        @Override public Optional<Washroom> getById(final String id) { return values.stream().filter(w -> w.id().equals(id)).findFirst(); }
        @Override public List<Washroom> getNearby(final double lat, final double lng, final double radius) { return values; }
        @Override public List<Washroom> getAll() { return values; }
    }

    private static final class SingleFilterRepository implements WashroomFilterRepository {
        private final Washroom washroom;
        private SingleFilterRepository(final Washroom washroom) { this.washroom = washroom; }
        @Override public Optional<Washroom> getById(final String id) { return Optional.ofNullable(washroom).filter(w -> w.id().equals(id)); }
        @Override public List<Washroom> getByIds(final Collection<String> ids) { return List.of(washroom); }
        @Override public List<Washroom> getNearby(final double lat, final double lng, final double radius) { return List.of(washroom); }
        @Override public List<Washroom> getAll() { return List.of(washroom); }
        @Override public List<Washroom> findMatching(final use_case.filter.WashroomFilterCriteria criteria) { return new ArrayList<>(List.of(washroom)); }
    }

    private static final class EmptyReviews implements ReviewRepository {
        @Override public List<Review> getReviewsForWashroom(final String id) { return List.of(); }
        @Override public ReviewSummary getSummary(final String id) { return ReviewSummary.empty(); }
        @Override public List<Review> getReviewsByUser(final String username) { return List.of(); }
        @Override public void save(final Review review) { }
    }
}
