import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import entity.GeoPoint;
import entity.MaintenanceIssue;
import entity.ReviewSummary;
import entity.Washroom;
import interface_adapter.account.AccountState;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.IsLoggedInViewModel;
import interface_adapter.account.change_password.ChangePasswordController;
import interface_adapter.account.change_password.ChangePasswordPresenter;
import interface_adapter.account.change_username.ChangeUsernameController;
import interface_adapter.account.change_username.ChangeUsernamePresenter;
import interface_adapter.account.delete_account.DeleteAccountController;
import interface_adapter.account.delete_account.DeleteAccountPresenter;
import interface_adapter.account.load_account.LoadAccountController;
import interface_adapter.account.load_account.LoadAccountPresenter;
import interface_adapter.account.personal_plan.PersonalPlanController;
import interface_adapter.account.personal_plan.PersonalPlanPresenter;
import interface_adapter.busyness.BusynessController;
import interface_adapter.busyness.BusynessPresenter;
import interface_adapter.busyness.BusynessViewModel;
import interface_adapter.common.AbstractViewModel;
import interface_adapter.directions.DirectionsController;
import interface_adapter.directions.DirectionsPresenter;
import interface_adapter.directions.MapViewModel;
import interface_adapter.filter.FilterController;
import interface_adapter.filter.FilterPresenter;
import interface_adapter.filter.FilterViewModel;
import interface_adapter.login.LoggedInViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.login.LoginViewModel;
import interface_adapter.login.SignupController;
import interface_adapter.login.SignupPresenter;
import interface_adapter.logout.LogoutController;
import interface_adapter.logout.LogoutPresenter;
import interface_adapter.moderate_reviews.ModerateReviewsController;
import interface_adapter.moderate_reviews.ModerateReviewsPresenter;
import interface_adapter.moderate_reviews.ModerateReviewsViewModel;
import interface_adapter.report_review.ReportReviewController;
import interface_adapter.report_review.ReportReviewPresenter;
import interface_adapter.report_review.ReportReviewViewModel;
import interface_adapter.sort_reviews.SortReviewsController;
import interface_adapter.sort_washrooms.SortWashroomController;
import interface_adapter.sort_washrooms.SortWashroomPresenter;
import interface_adapter.sort_washrooms.SortWashroomViewModel;
import interface_adapter.status_report.StatusReportController;
import interface_adapter.status_report.StatusReportPresenter;
import interface_adapter.status_report.StatusReportViewModel;
import interface_adapter.view_reviews.ReviewsViewModel;
import interface_adapter.view_reviews.ViewReviewsController;
import interface_adapter.view_reviews.ViewReviewsPresenter;
import interface_adapter.view_reviews.WashroomListViewModel;
import interface_adapter.vote_helpful.VoteHelpfulController;
import interface_adapter.write_review.WriteReviewController;
import interface_adapter.write_review.WriteReviewPresenter;
import interface_adapter.write_review.WriteReviewViewModel;
import use_case.account.change_password.ChangePasswordInputData;
import use_case.account.change_password.ChangePasswordOutputData;
import use_case.account.change_username.ChangeUsernameInputData;
import use_case.account.change_username.ChangeUsernameOutputData;
import use_case.account.delete_account.DeleteAccountOutputData;
import use_case.account.load_account.LoadAccountOutputData;
import use_case.account.personal_plan.PersonalPlanOutputData;
import use_case.busyness.BusynessStatsInputData;
import use_case.busyness.BusynessStatsOutputData;
import use_case.directions.GetDirectionsInputData;
import use_case.directions.GetDirectionsOutputData;
import use_case.filter.FilterInputData;
import use_case.filter.FilterOutputData;
import use_case.moderate_reviews.ModerateReviewsInputData;
import use_case.moderate_reviews.ModerateReviewsOutputData;
import use_case.moderate_reviews.ReportedReview;
import use_case.report_review.ReportReviewInputData;
import use_case.report_review.ReportReviewOutputData;
import use_case.signup.SignupInputData;
import use_case.signup.SignupOutputData;
import use_case.sort_review.SortReviewInputData;
import use_case.sort_washrooms.SortWashroomInputData;
import use_case.sort_washrooms.SortWashroomsOutputData;
import use_case.status_report.SubmitStatusReportInputData;
import use_case.status_report.SubmitStatusReportOutputData;
import use_case.view_reviews.ViewReviewsInputData;
import use_case.view_reviews.ViewReviewsOutputData;
import use_case.vote_helpful.VoteHelpfulInputData;
import use_case.write_review.WriteReviewInputData;
import use_case.write_review.WriteReviewOutputData;

/** Tests the adapter boundary mappings and state transitions independently of Swing. */
class InterfaceAdapterCoverageTest {
    private static final Washroom WASHROOM = new Washroom("w1", "Main washroom | West wing washrooms",
        new entity.Building("BA", "Bahen", 43.66, -79.39), "2", true, Washroom.Gender.ALL_GENDER, 3, 2,
        "near elevators", new ReviewSummary(4, 3, 2));

    @Test
    void viewModelsKeepStateAndNotifyListeners() {
        final LoginViewModel login = new LoginViewModel();
        final AtomicReference<Object> event = new AtomicReference<>();
        login.addPropertyChangeListener(event::set);
        final LoginViewModel.State loginState = new LoginViewModel.State(true, "alice", "ok");
        login.setState(loginState);
        assertSame(loginState, login.getState());
        assertNotNull(event.get());

        final ReviewsViewModel reviews = new ReviewsViewModel();
        final ViewReviewsOutputData.ReviewDisplay voted = new ViewReviewsOutputData.ReviewDisplay("r1", 4,
            "good", 0, LocalDate.now(), "alice", false, false);
        final ViewReviewsOutputData.ReviewDisplay alreadyVoted = new ViewReviewsOutputData.ReviewDisplay("r2", 2,
            "bad", 1, LocalDate.now(), "bob", true, false);
        reviews.setState(new ReviewsViewModel.State("w1", "Bahen", "West", 4, 3, 2, 3, 2,
            List.of(voted, alreadyVoted), null));
        reviews.toggleHelpfulVote("r1");
        assertEquals(1, reviews.getState().reviews().getFirst().helpfulCount());
        assertTrue(reviews.getState().reviews().getFirst().votedByCurrentUser());
        reviews.toggleHelpfulVote("r2");
        assertEquals(0, reviews.getState().reviews().get(1).helpfulCount());
        assertFalse(reviews.getState().reviews().get(1).votedByCurrentUser());

        final WashroomListViewModel.Item first = new WashroomListViewModel.Item("a", "A", "d", 4, 10, true);
        final WashroomListViewModel.Item second = new WashroomListViewModel.Item("b", "B", "d", 5, 20, false);
        assertEquals("a", WashroomListViewModel.Item.BY_DISTANCE.compare(first, second) < 0 ? "a" : "b");
        assertTrue(WashroomListViewModel.Item.BY_RATING.compare(first, second) > 0);
        assertEquals("a", WashroomListViewModel.Item.BY_ALPHABETICAL.compare(first, second) < 0 ? "a" : "b");
        final WashroomListViewModel.State listState = new WashroomListViewModel.State(List.of(first), "a", "Nearest",
            false);
        assertEquals(first, listState.items().getFirst());

        final BusynessStatsOutputData.HourBucket bucket = new BusynessStatsOutputData.HourBucket(9, 3, 4, "reports");
        final BusynessViewModel busyness = new BusynessViewModel();
        busyness.setState(new BusynessViewModel.State(List.of(bucket), "note"));
        assertEquals(bucket, busyness.getState().buckets().getFirst());
        assertEquals("note", busyness.getState().note());
        final MapViewModel.State mapState = new MapViewModel.State(true, List.of(new GeoPoint(1, 2)), "1 m", "1 min",
            "ok");
        final MapViewModel map = new MapViewModel();
        map.setState(mapState);
        assertEquals(mapState, map.getState());
        assertEquals(List.of(new GeoPoint(1, 2)), map.getState().points());

        final FilterViewModel filter = new FilterViewModel();
        filter.setState(new FilterViewModel.State(true, List.of(WASHROOM), "ok"));
        assertTrue(filter.getState().success());
        final ReportReviewViewModel report = new ReportReviewViewModel();
        report.setState(new ReportReviewViewModel.State("done", true, true));
        assertTrue(report.getState().submitted());
        final ModerateReviewsViewModel moderate = new ModerateReviewsViewModel();
        final ReportedReview reported = new ReportedReview("r1", "Bahen", "alice", LocalDate.now(), 4, "text",
            List.of(new ReportedReview.ReasonCount("Spam", 2)),
            List.of(new ReportedReview.AdditionalDetail("Spam", "details")));
        moderate.setState(new ModerateReviewsViewModel.State(List.of(reported), "queue"));
        assertEquals(1, moderate.getState().reportedReviews().size());
        assertEquals(2, reported.totalReports());
        assertEquals(1, reported.additionalDetailsCount());
        final StatusReportViewModel status = new StatusReportViewModel();
        status.setState(new StatusReportViewModel.State(true, 3.5, "saved"));
        assertEquals(3.5, status.getState().currentBusyness());
        final WriteReviewViewModel write = new WriteReviewViewModel();
        write.setState(new WriteReviewViewModel.State(true, "posted"));
        assertTrue(write.getState().success());

        final AccountState accountState = new AccountState("alice", "plan", true, "u", true, "p", true, "d", true,
            "plan ok");
        accountState.setUsername("bob");
        accountState.setPersonalPlan("new plan");
        accountState.setChangeUsernameSuccess(false);
        accountState.setChangeUsernameMessage("u2");
        accountState.setChangePasswordSuccess(false);
        accountState.setChangePasswordMessage("p2");
        accountState.setDeleteAccountSuccess(false);
        accountState.setDeleteAccountMessage("d2");
        accountState.setPersonalPlanSuccess(false);
        accountState.setPersonalPlanMessage("plan2");
        assertEquals("bob", accountState.getUsername());
        assertEquals("new plan", accountState.getPersonalPlan());
        assertEquals("plan2", accountState.getPersonalPlanMessage());
        accountState.exitResetState();
        assertFalse(accountState.getChangeUsernameSuccess());
        accountState.logoutResetState();
        assertEquals("", accountState.getUsername());
        assertEquals("", accountState.getPersonalPlan());
        final AccountViewModel account = new AccountViewModel();
        account.setUsername("alice");
        account.setPersonalPlan("plan");
        account.setChangeUsernameSuccess(true);
        account.setChangeUsernameMessage("ok");
        account.setChangePasswordSuccess(true);
        account.setChangePasswordMessage("ok");
        account.setDeleteAccountSuccess(true);
        account.setDeleteAccountMessage("ok");
        account.setPersonalPlanSuccess(true);
        account.setPersonalPlanMessage("ok");
        assertEquals("alice", account.getUsername());
        assertEquals("plan", account.getPersonalPlan());
        assertTrue(account.getChangeUsernameSuccess() && account.getChangePasswordSuccess()
            && account.getDeleteAccountSuccess() && account.getPersonalPlanSuccess());
        account.addPropertyChangeListener(event::set);
        assertEquals("ok", account.getChangePasswordMessage());
        assertEquals("ok", account.getDeleteAccountMessage());
        assertEquals("ok", account.getPersonalPlanMessage());
        account.exitResetState();
        account.logoutResetState();
        final IsLoggedInViewModel logged = new IsLoggedInViewModel();
        logged.setUsername("alice");
        logged.setIsLoggedIn(true);
        assertEquals("alice", logged.getUsername());
        assertTrue(logged.getIsLoggedIn());
        logged.addPropertyChangeListener(event::set);
        logged.setUsername("bob");
        logged.setIsLoggedIn(false);
        assertFalse(logged.getIsLoggedIn());
        final interface_adapter.account.IsLoggedInState loggedState =
            new interface_adapter.account.IsLoggedInState(true, "state-user");
        loggedState.addPropertyChangeListener(event::set);
        loggedState.setUsername("state-user-2");
        loggedState.setIsLoggedIn(false);
        assertEquals("state-user-2", loggedState.getUsername());

        final AbstractViewModel<String> generic = new AbstractViewModel<>("initial") { };
        final AtomicReference<Object> genericEvent = new AtomicReference<>();
        generic.addPropertyChangeListener(genericEvent::set);
        generic.setState("next");
        generic.removePropertyChangeListener(genericEvent::set);
        assertEquals("next", generic.getState());
    }

    @Test
    void presentersMapBothOutcomes() {
        final LoginViewModel login = new LoginViewModel();
        final LoggedInViewModel loggedIn = new LoggedInViewModel();
        final IsLoggedInViewModel session = new IsLoggedInViewModel();
        final LoginPresenter loginPresenter = new LoginPresenter(login, loggedIn, session);
        loginPresenter.present(new use_case.login.LoginOutputData(false, "", false, "bad"));
        assertFalse(login.getState().success());
        loginPresenter.present(new use_case.login.LoginOutputData(true, "alice", true, "ok"));
        assertTrue(login.getState().success() && loggedIn.getState().moderator() && session.getIsLoggedIn());
        final SignupPresenter signup = new SignupPresenter(login, loggedIn);
        signup.present(new SignupOutputData(false, "", "taken"));
        signup.present(new SignupOutputData(true, "new-user", "created"));
        assertEquals("new-user", loggedIn.getState().username());
        final LogoutPresenter logout = new LogoutPresenter(session, login, loggedIn);
        logout.present();
        assertFalse(session.getIsLoggedIn());
        assertEquals("Guest", loggedIn.getState().username());

        final AccountViewModel account = new AccountViewModel();
        final IsLoggedInViewModel session2 = new IsLoggedInViewModel();
        final ChangeUsernamePresenter username = new ChangeUsernamePresenter(account, session2);
        username.present(new ChangeUsernameOutputData(true, "renamed", "alice2"));
        assertEquals("alice2", account.getUsername());
        assertEquals("renamed", account.getChangeUsernameMessage());
        final ChangePasswordPresenter password = new ChangePasswordPresenter(account);
        password.present(new ChangePasswordOutputData(false, "wrong password"));
        assertFalse(account.getChangePasswordSuccess());
        final DeleteAccountPresenter delete = new DeleteAccountPresenter(account, session2);
        session2.setIsLoggedIn(true);
        delete.present(new DeleteAccountOutputData(false, "failed"));
        assertTrue(session2.getIsLoggedIn());
        delete.present(new DeleteAccountOutputData(true, "deleted"));
        assertFalse(session2.getIsLoggedIn());
        final LoadAccountPresenter load = new LoadAccountPresenter(account);
        load.present(new LoadAccountOutputData("loaded", "calendar"));
        assertEquals("calendar", account.getPersonalPlan());
        final PersonalPlanPresenter plan = new PersonalPlanPresenter(account);
        plan.present(new PersonalPlanOutputData(false, "no plan", ""));
        plan.present(new PersonalPlanOutputData(true, "ready", "route plan"));
        assertEquals("route plan", account.getPersonalPlan());

        final FilterViewModel filter = new FilterViewModel();
        final WashroomListViewModel list = new WashroomListViewModel();
        final FilterPresenter filterPresenter = new FilterPresenter(filter, list, Runnable::run);
        filterPresenter.present(new FilterOutputData(true, List.of(WASHROOM), 43.66, -79.39));
        assertTrue(filter.getState().success());
        assertEquals("West wing washrooms", list.getState().items().getFirst().description());
        filterPresenter.presentError("invalid filter");
        assertEquals("invalid filter", filter.getState().message());
        final Washroom plainWashroom = new Washroom("w2", "Plain", WASHROOM.building(), "2", true,
            Washroom.Gender.ALL_GENDER, 3, 2, "inside", new ReviewSummary(4, 3, 2));
        filterPresenter.present(new FilterOutputData(true, List.of(plainWashroom), 43.66, -79.39));
        assertEquals("Plain", list.getState().items().getFirst().description());
        final SortWashroomViewModel sort = new SortWashroomViewModel();
        final SortWashroomPresenter sortPresenter = new SortWashroomPresenter(list, sort, Runnable::run);
        sortPresenter.present(new SortWashroomsOutputData(true, List.of(WASHROOM), 43.66, -79.39));
        assertTrue(sort.getState().success());
        sortPresenter.present(new SortWashroomsOutputData(true, List.of(plainWashroom), 43.66, -79.39));
        assertEquals("Plain", list.getState().items().getFirst().description());

        final MapViewModel map = new MapViewModel();
        final DirectionsPresenter directions = new DirectionsPresenter(map, Runnable::run);
        directions.present(new GetDirectionsOutputData(true, List.of(new GeoPoint(1, 2)), 100, 30, "ok"));
        assertEquals("100 m", map.getState().distance());
        assertEquals("1 min", map.getState().duration());
        directions.present(new GetDirectionsOutputData(false, List.of(), 0, 0, "failed"));
        assertEquals("", map.getState().distance());
        final BusynessViewModel busyness = new BusynessViewModel();
        new BusynessPresenter(busyness).present(new BusynessStatsOutputData(List.of(), "reports"));
        assertEquals("reports", busyness.getState().note());
        final StatusReportViewModel status = new StatusReportViewModel();
        new StatusReportPresenter(status).present(new SubmitStatusReportOutputData(true, 4, "saved"));
        assertEquals(4, status.getState().currentBusyness());
        final ReportReviewViewModel report = new ReportReviewViewModel();
        new ReportReviewPresenter(report).present(new ReportReviewOutputData(true, "reported"));
        assertTrue(report.getState().submitted());
        final ModerateReviewsViewModel moderate = new ModerateReviewsViewModel();
        new ModerateReviewsPresenter(moderate).present(new ModerateReviewsOutputData(List.of(), "empty"));
        assertEquals("empty", moderate.getState().message());
        final WriteReviewViewModel write = new WriteReviewViewModel();
        new WriteReviewPresenter(write).present(new WriteReviewOutputData(true, "posted"));
        assertTrue(write.getState().success());
        final ViewReviewsPresenter reviewsPresenter = new ViewReviewsPresenter(new ReviewsViewModel());
        reviewsPresenter.presentError("missing");
        final ReviewsViewModel reviews = new ReviewsViewModel();
        new ViewReviewsPresenter(reviews).present(new ViewReviewsOutputData("w1", "Bahen", "desc", 4,
            3, 1, 2, 1, List.of()));
        assertEquals("w1", reviews.getState().washroomId());
    }

    @Test
    void controllersForwardInputs() throws InterruptedException {
        final AtomicReference<Object> received = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        new LoginController(input -> { received.set(input); latch.countDown(); }).execute("alice", "secret");
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(new use_case.login.LoginInputData("alice", "secret"), received.get());
        final CountDownLatch signupLatch = new CountDownLatch(1);
        new SignupController(input -> { received.set(input); signupLatch.countDown(); }).execute("bob", "pw");
        assertTrue(signupLatch.await(2, TimeUnit.SECONDS));
        assertEquals(new SignupInputData("bob", "pw"), received.get());
        final CountDownLatch logoutLatch = new CountDownLatch(1);
        new LogoutController(logoutLatch::countDown).execute();
        assertTrue(logoutLatch.await(2, TimeUnit.SECONDS));

        final AtomicReference<Object> sync = new AtomicReference<>();
        new ChangeUsernameController(input -> sync.set(input)).execute("new");
        new ChangePasswordController(input -> sync.set(input)).execute("new", "confirm");
        assertEquals(new ChangePasswordInputData("new", "confirm"), sync.get());
        final CountDownLatch noArg1 = new CountDownLatch(1);
        new DeleteAccountController(noArg1::countDown).execute();
        assertTrue(noArg1.await(2, TimeUnit.SECONDS));
        final CountDownLatch noArg2 = new CountDownLatch(1);
        new LoadAccountController(noArg2::countDown).execute();
        assertTrue(noArg2.await(2, TimeUnit.SECONDS));
        new PersonalPlanController(input -> sync.set(input)).execute("calendar", "2", "Fall");
        assertEquals(new use_case.account.personal_plan.PersonalPlanInputData("calendar", "2", "Fall"), sync.get());

        new BusynessController(input -> sync.set(input)).execute("w1", "BA", DayOfWeek.MONDAY);
        assertEquals(new BusynessStatsInputData("w1", "BA", DayOfWeek.MONDAY), sync.get());
        new DirectionsController(input -> sync.set(input)).execute(1.2, 3.4, "w1");
        assertEquals(new GetDirectionsInputData(1.2, 3.4, "w1"), sync.get());
        new ViewReviewsController(input -> sync.set(input)).execute("w1", "alice");
        assertEquals(new ViewReviewsInputData("w1", "alice"), sync.get());
        new StatusReportController(input -> sync.set(input)).execute("w1", 3, 4, MaintenanceIssue.NONE, "alice");
        assertEquals(new SubmitStatusReportInputData("w1", 3, 4, MaintenanceIssue.NONE, "alice"), sync.get());
        new ReportReviewController(input -> sync.set(input)).report("r1", "alice", List.of("Spam"), "details");
        assertEquals(new ReportReviewInputData("r1", "alice", List.of("Spam"), "details"), sync.get());
        new ModerateReviewsController(new use_case.moderate_reviews.ModerateReviewsInputBoundary() {
            @Override public void loadReportedReviews() { }
            @Override public void removeReview(final ModerateReviewsInputData input) { sync.set(input); }
            @Override public void dismissReports(final ModerateReviewsInputData input) { sync.set(input); }
        }).remove("r1", "mod");
        assertEquals(new ModerateReviewsInputData("r1", "mod"), sync.get());
        new ModerateReviewsController(new use_case.moderate_reviews.ModerateReviewsInputBoundary() {
            @Override public void loadReportedReviews() { sync.set("loaded"); }
            @Override public void removeReview(final ModerateReviewsInputData input) { }
            @Override public void dismissReports(final ModerateReviewsInputData input) { sync.set(input); }
        }).load();
        assertEquals("loaded", sync.get());
        new ModerateReviewsController(new use_case.moderate_reviews.ModerateReviewsInputBoundary() {
            @Override public void loadReportedReviews() { }
            @Override public void removeReview(final ModerateReviewsInputData input) { }
            @Override public void dismissReports(final ModerateReviewsInputData input) { sync.set(input); }
        }).dismiss("r1", "mod");
        assertEquals(new ModerateReviewsInputData("r1", "mod"), sync.get());
        new WriteReviewController(input -> sync.set(input)).execute("w1", "alice", 4, 5, "good");
        assertEquals(new WriteReviewInputData("w1", "alice", 4, 5, "good"), sync.get());
        new VoteHelpfulController(input -> sync.set(input)).toggle("r1", "alice");
        assertEquals(new VoteHelpfulInputData("r1", "alice"), sync.get());

        final CountDownLatch filterLatch = new CountDownLatch(1);
        final AtomicReference<FilterInputData> filterInput = new AtomicReference<>();
        new FilterController(input -> { filterInput.set(input); filterLatch.countDown(); }).execute(5, 1, true, true,
            true, false, "BA", Washroom.Gender.WOMEN, 43.6, -79.4);
        assertTrue(filterLatch.await(2, TimeUnit.SECONDS));
        assertNotNull(filterInput.get());
        final CountDownLatch nullGenderLatch = new CountDownLatch(1);
        new FilterController(input -> { filterInput.set(input); nullGenderLatch.countDown(); }).execute(5, 1, false,
            false, false, false, "ignored", null, 0, 0);
        assertTrue(nullGenderLatch.await(2, TimeUnit.SECONDS));
        assertNotNull(filterInput.get());
        for (final Washroom.Gender gender : Washroom.Gender.values()) {
            final CountDownLatch genderLatch = new CountDownLatch(1);
            new FilterController(input -> genderLatch.countDown()).execute(1, 1, false, false, false, false, "", gender,
                0, 0);
            assertTrue(genderLatch.await(2, TimeUnit.SECONDS));
        }

        final CountDownLatch sortReviewLatch = new CountDownLatch(1);
        final AtomicReference<SortReviewInputData> sortReview = new AtomicReference<>();
        new SortReviewsController((input) -> { sortReview.set(input); sortReviewLatch.countDown(); }).execute("Newest",
            "w1");
        assertTrue(sortReviewLatch.await(2, TimeUnit.SECONDS));
        assertEquals("w1", sortReview.get().currentWashroom());
        final CountDownLatch sortWashroomLatch = new CountDownLatch(1);
        final AtomicReference<SortWashroomInputData> sortWashroom = new AtomicReference<>();
        new SortWashroomController(input -> { sortWashroom.set(input); sortWashroomLatch.countDown(); }).execute("Nearest",
            List.of("w1"), 1, 2);
        assertTrue(sortWashroomLatch.await(2, TimeUnit.SECONDS));
        assertEquals(List.of("w1"), sortWashroom.get().washroomIdList());
        assertEquals(43.66, WASHROOM.building().latitude());
    }
}
