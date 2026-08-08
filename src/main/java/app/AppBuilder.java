package app;

import data_access.DBDataAccessObject;
import data_access.enrollment.DBEnrollmentDataAccessObject;
import data_access.review.DBReviewDataAccessObject;
import data_access.route.GraphhopperGeocodingDataAccessObject;
import data_access.route.GraphhopperRouteDataAccessObject;
import data_access.status.DBStatusReportDataAccessObject;
import data_access.user.DBUserDataAccessObject;
import data_access.washroom.DBWashroomDataAccessObject;
import entity.Washroom;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.IsLoggedInViewModel;
import interface_adapter.account.change_password.ChangePasswordController;
import interface_adapter.account.change_password.ChangePasswordPresenter;
import interface_adapter.account.change_username.ChangeUsernameController;
import interface_adapter.account.change_username.ChangeUsernamePresenter;
import interface_adapter.account.delete_account.DeleteAccountController;
import interface_adapter.account.delete_account.DeleteAccountPresenter;
import interface_adapter.account.personal_plan.PersonalPlanController;
import interface_adapter.account.personal_plan.PersonalPlanPresenter;
import interface_adapter.busyness.BusynessController;
import interface_adapter.busyness.BusynessPresenter;
import interface_adapter.busyness.BusynessViewModel;
import interface_adapter.directions.DirectionsController;
import interface_adapter.directions.DirectionsPresenter;
import interface_adapter.directions.MapViewModel;
import interface_adapter.filter.FilterController;
import interface_adapter.filter.FilterPresenter;
import interface_adapter.filter.FilterViewModel;
import interface_adapter.login.*;
import interface_adapter.moderate_reviews.ModerateReviewsController;
import interface_adapter.moderate_reviews.ModerateReviewsPresenter;
import interface_adapter.moderate_reviews.ModerateReviewsViewModel;
import interface_adapter.report_review.ReportReviewController;
import interface_adapter.report_review.ReportReviewPresenter;
import interface_adapter.report_review.ReportReviewViewModel;
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
import use_case.account.change_password.ChangePasswordInteractor;
import use_case.account.change_username.ChangeUsernameInteractor;
import use_case.account.delete_account.DeleteAccountInteractor;
import use_case.account.personal_plan.PersonalPlanInteractor;
import use_case.busyness.BusynessStatsInteractor;
import use_case.directions.GetDirectionsInteractor;
import use_case.filter.FilterInteractor;
import use_case.login.LoginInteractor;
import use_case.moderate_reviews.ModerateReviewsInteractor;
import use_case.report_review.ReportReviewInteractor;
import use_case.signup.SignupInteractor;
import use_case.sort_washrooms.SortWashroomInteractor;
import use_case.status_report.SubmitStatusReportInteractor;
import use_case.view_reviews.ViewReviewsInteractor;
import use_case.vote_helpful.VoteHelpfulInteractor;
import use_case.write_review.WriteReviewInteractor;
import view.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Composition root: the only class that selects concrete database and external-service adapters.
 */
public final class AppBuilder {
    private static final String MAIN = "main", REVIEWS = "reviews", LOGIN = "login", STATUS = "status", BUSYNESS = "busyness", ACCOUNT = "account", MODERATE = "moderate";
    private static final Set<String> JSON_WASHROOM_NAMES = loadJsonWashroomNames();

    private static void showLoadedFrame(JFrame frame) {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void requestDirections(MainView main, DirectionsController controller, String washroomId) {
        main.showRouting();
        CompletableFuture.runAsync(() -> controller.execute(main.latitude(), main.longitude(), washroomId));
    }

    private static void loadMainDataAsync(DBWashroomDataAccessObject washrooms, DBReviewDataAccessObject reviews,
                                          DBStatusReportDataAccessObject reports, boolean seedData, Runnable loadModerator,
                                          MainView main, WashroomListViewModel listModel,
                                          AtomicReference<List<Washroom>> displayedWashrooms,
                                          double originLat, double originLng, Runnable onLoaded) {
        Thread loader = new Thread(() -> {
            MainData data = null;
            Throwable failure = null;
            try {
                if (seedData) seedInitialData(washrooms, reviews, reports);
                List<Washroom> availableWashrooms = jsonWashrooms(washrooms);
                List<MainView.HeatmapData> heatmapData = heatmapData(availableWashrooms, reports);
                try {
                    loadModerator.run();
                } catch (RuntimeException moderatorFailure) {
                    // A malformed moderation record must not prevent the main map from opening.
                    System.err.println("Moderator queue did not load: " + moderatorFailure.getMessage());
                }
                data = new MainData(availableWashrooms, heatmapData);
            } catch (Throwable exception) {
                failure = exception;
            }
            MainData completedData = data;
            Throwable completedFailure = failure;
            SwingUtilities.invokeLater(() -> {
                MainData loaded = completedData;
                Throwable loadFailure = completedFailure;
                if (loadFailure != null) {
                    listModel.setState(new WashroomListViewModel.State(List.of(), "", "Alphabetical", false));
                    onLoaded.run();
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(main),
                            "Could not load the washroom map. Check the database connection and try again.",
                            "FlushID", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                displayedWashrooms.set(loaded.washrooms());
                refreshMainWashrooms(loaded.washrooms(), main, listModel, originLat, originLng);
                main.setHeatmapData(loaded.heatmapData());
                onLoaded.run();
            });
        }, "FlushID initial data loader");
        loader.setDaemon(false);
        loader.start();
    }

    /**
     * Runs the optional baseline-data maintenance away from Swing's event thread.
     */
    private static void seedInitialData(DBWashroomDataAccessObject washrooms, DBReviewDataAccessObject reviews,
                                        DBStatusReportDataAccessObject reports) {
        washrooms.ensurePerformanceIndexes();
        reviews.ensurePerformanceIndexes();
        reports.ensurePerformanceIndexes();
        List<Washroom> jsonWashrooms = jsonWashrooms(washrooms);
        reviews.ensureJsonReviews(jsonWashrooms);
        reports.ensureJsonHourlyReports(jsonWashrooms);
    }

    private static void refreshMainWashrooms(List<Washroom> availableWashrooms, MainView main,
                                             WashroomListViewModel listModel, double originLat, double originLng) {
        main.setWashrooms(availableWashrooms);
        List<WashroomListViewModel.Item> items = availableWashrooms.stream().map(w ->
                new WashroomListViewModel.Item(w.id(), w.building().name(), listDescription(w), w.reviewSummary().averageRating(),
                        (int) Math.round(distance(originLat, originLng, w.building().latitude(), w.building().longitude())),
                        w.accessible())).toList();
        String selectedId = main.selectedId().isBlank() && !items.isEmpty() ? items.getFirst().id() : main.selectedId();
        listModel.setState(new WashroomListViewModel.State(items, selectedId, "Sort by: Nearest", false));
    }

    /**
     * Refreshes heatmap data off the UI thread after a live status submission.
     */
    private static void refreshHeatmapAsync(List<Washroom> washrooms,
                                            DBStatusReportDataAccessObject reports, MainView main) {
        CompletableFuture.supplyAsync(() -> heatmapData(washrooms, reports))
                .thenAccept(data -> SwingUtilities.invokeLater(() -> main.setHeatmapData(data)));
    }

    /**
     * Builds the current-hour heatmap in one status-report aggregation.
     */
    private static List<MainView.HeatmapData> heatmapData(List<Washroom> washrooms,
                                                          DBStatusReportDataAccessObject reports) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, entity.StatusReport> currentHour = reports.getCurrentHourForWashrooms(
                washrooms.stream().map(Washroom::id).toList(), now.getHour());
        return washrooms.stream().map(washroom ->
                        Optional.ofNullable(currentHour.get(washroom.id()))
                                .map(report -> new MainView.HeatmapData(washroom.id(), report.busyness(), report.cleanliness()))
                                .orElseGet(() -> new MainView.HeatmapData(washroom.id(), Double.NaN, Double.NaN)))
                .toList();
    }

    /**
     * A new review changes the aggregate rating for only its washroom.  Keep the
     * already-loaded list and patch that one row instead of reloading every
     * washroom (which also recalculates every review summary from MongoDB).
     */
    private static void updateWashroomListRating(WashroomListViewModel listModel, String washroomId,
                                                 double rating) {
        WashroomListViewModel.State current = listModel.getState();
        List<WashroomListViewModel.Item> updated = current.items().stream()
                .map(item -> item.id().equals(washroomId)
                        ? new WashroomListViewModel.Item(item.id(), item.name(), item.description(), rating,
                        item.distanceMeters(), item.accessible())
                        : item)
                .toList();
        listModel.setState(new WashroomListViewModel.State(updated, current.selectedId(),
                current.sortLabel(), current.routeVisible()));
    }

    private static Optional<Washroom> selected(DBWashroomDataAccessObject washrooms, MainView main) {
        return main.selectedId().isBlank() ? Optional.empty() : washrooms.getById(main.selectedId());
    }

    private static void noWashroom(Component parent) {
        JOptionPane.showMessageDialog(parent, "Select a washroom from the list or map before continuing.",
                "Select a washroom", JOptionPane.WARNING_MESSAGE);
    }

    private static String listDescription(Washroom washroom) {
        String name = washroom.name();
        int separator = name.indexOf('|');
        String description = separator >= 0 ? name.substring(separator + 1) : name;
        return description.replaceAll("(?i)\\bwashrooms?\\b", "")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    private static Set<String> loadJsonWashroomNames() {
        InputStream input = AppBuilder.class.getResourceAsStream("/data/washrooms.json");
        if (input == null) throw new IllegalStateException("Missing data/washrooms.json resource.");
        try (input; JsonReader reader = Json.createReader(input)) {
            return Set.copyOf(reader.readArray().stream()
                    .map(value -> ((JsonObject) value).getString("name"))
                    .toList());
        } catch (Exception failure) {
            throw new IllegalStateException("Could not load data/washrooms.json.", failure);
        }
    }

    private static List<Washroom> jsonWashrooms(DBWashroomDataAccessObject washrooms) {
        return washrooms.getByNames(JSON_WASHROOM_NAMES);
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank())
            throw new IllegalStateException("Set the " + name + " environment variable before starting FlushID.");
        return value;
    }

    private static double distance(double a, double b, double c, double d) {
        double x = Math.toRadians(d - b) * Math.cos(Math.toRadians((a + c) / 2));
        double y = Math.toRadians(c - a);
        return Math.sqrt(x * x + y * y) * 6_371_000;
    }

    /**
     * Builds the application from the data already stored in MongoDB.
     */
    public JFrame build() {
        return build(false, AppBuilder::showLoadedFrame);
    }

    /**
     * Builds the application and asynchronously verifies/seeds its baseline data.
     */
    public JFrame buildAndSeed() {
        return build(true, AppBuilder::showLoadedFrame);
    }

    /**
     * Invokes {@code onLoaded} on Swing's event thread once the initial screen is ready to show.
     */
    public JFrame buildAndSeed(Consumer<JFrame> onLoaded) {
        return build(true, onLoaded == null ? AppBuilder::showLoadedFrame : onLoaded);
    }

    private JFrame build(boolean seedData, Consumer<JFrame> onLoaded) {
        String graphhopperKey = requiredEnvironment(GraphhopperRouteDataAccessObject.API_KEY_ENV);
        DBDataAccessObject connection = DBDataAccessObject.fromEnvironment();

        var database = connection.database();
        var washrooms = new DBWashroomDataAccessObject(database);
        var reviews = new DBReviewDataAccessObject(database);
        var users = new DBUserDataAccessObject(database);
        users.ensureModerator("frog");     // grant moderator to known accounts
        users.ensureModerator("sheena_q");
        var reports = new DBStatusReportDataAccessObject(database);
        var routes = new GraphhopperRouteDataAccessObject(graphhopperKey);
        var geocoding = new GraphhopperGeocodingDataAccessObject(graphhopperKey);
        var enrollment = new DBEnrollmentDataAccessObject(database);

        var isLoggedIn = new IsLoggedInViewModel();
        var reviewsModel = new ReviewsViewModel();
        var writeReviewModel = new WriteReviewViewModel();
        var listModel = new WashroomListViewModel();
        var loginModel = new LoginViewModel();
        var loggedInModel = new LoggedInViewModel();
        var accountModel = new AccountViewModel();
        var statusModel = new StatusReportViewModel();
        var busynessModel = new BusynessViewModel();
        var mapModel = new MapViewModel();
        var reportReviewModel = new ReportReviewViewModel();
        var moderateModel = new ModerateReviewsViewModel();
        var filterModel = new FilterViewModel();
        var sortWashroomModel = new SortWashroomViewModel();

        var reviewController = new ViewReviewsController(new ViewReviewsInteractor(reviews, washrooms, reviews, reviews, new ViewReviewsPresenter(reviewsModel)));
        var writeReviewController = new WriteReviewController(new WriteReviewInteractor(reviews, new WriteReviewPresenter(writeReviewModel)));
        var voteController = new VoteHelpfulController(new VoteHelpfulInteractor(reviews));
        var reportController = new ReportReviewController(new ReportReviewInteractor(reviews, new ReportReviewPresenter(reportReviewModel)));
        var moderateController = new ModerateReviewsController(new ModerateReviewsInteractor(reviews, reviews, washrooms, users, new ModerateReviewsPresenter(moderateModel)));
        Supplier<String> currentUser = () -> loggedInModel.getState().username();
        var loginController = new LoginController(new LoginInteractor(users, new LoginPresenter(loginModel, loggedInModel, isLoggedIn)));
        var signupController = new SignupController(new SignupInteractor(users, new SignupPresenter(loginModel, loggedInModel)));
        var statusController = new StatusReportController(new SubmitStatusReportInteractor(reports, new StatusReportPresenter(statusModel)));
        var busynessController = new BusynessController(new BusynessStatsInteractor(reports, enrollment, new BusynessPresenter(busynessModel)));
        var directionsController = new DirectionsController(new GetDirectionsInteractor(washrooms, routes, new DirectionsPresenter(mapModel)));
        var changeUsernameController = new ChangeUsernameController(new ChangeUsernameInteractor(users, new ChangeUsernamePresenter(accountModel, isLoggedIn)));
        var changePasswordController = new ChangePasswordController(new ChangePasswordInteractor(users, new ChangePasswordPresenter(accountModel)));
        var deleteAccountController = new DeleteAccountController(new DeleteAccountInteractor(users, new DeleteAccountPresenter(accountModel, isLoggedIn)));
        var personalPlanController = new PersonalPlanController(new PersonalPlanInteractor(users, new PersonalPlanPresenter(accountModel)));
        var filterController = new FilterController(new FilterInteractor(washrooms, reviews, reports, users,
                new FilterPresenter(filterModel, listModel, mapModel), JSON_WASHROOM_NAMES));
        var sortWashroomController = new SortWashroomController(new SortWashroomInteractor(washrooms, new SortWashroomPresenter(listModel, sortWashroomModel)));

        JFrame frame = new JFrame("FlushID — U of T washroom finder");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setSize(1060, 700);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                connection.close();
            }
        });
        CardLayout layout = new CardLayout();
        JPanel cards = new JPanel(layout);
        MainView main = new MainView(listModel, mapModel, filterModel, sortWashroomModel, isLoggedIn);
        AtomicReference<List<Washroom>> displayedWashrooms = new AtomicReference<>(List.of());
        main.setAddressLookup(geocoding::lookup);
        double originLat = 43.6629, originLng = -79.3957;

        ReadReviewsView readReviews = new ReadReviewsView(reviewsModel);
        LoginPanel login = new LoginPanel(loginModel, loginController);

        AccountView account = new AccountView(
                accountModel,
                changeUsernameController,
                changePasswordController,
                deleteAccountController,
                personalPlanController
        );
        StatusReportView status = new StatusReportView(statusModel);
        BusynessChartView busyness = new BusynessChartView(busynessModel);
        ReportedReviewsView moderate = new ReportedReviewsView(moderateModel);
        cards.add(main, MAIN);
        cards.add(readReviews, REVIEWS);
        cards.add(login, LOGIN);
        cards.add(account, ACCOUNT);
        cards.add(status, STATUS);
        cards.add(busyness, BUSYNESS);
        cards.add(moderate, MODERATE);
        frame.setContentPane(cards);
        layout.show(cards, LOGIN);

        Runnable showMain = () -> layout.show(cards, MAIN);
        main.setOnReviews(id -> {
            reviewController.execute(id, currentUser.get());
            layout.show(cards, REVIEWS);
        });
        main.setOnModerator(() -> {
            moderate.setModeratorUsername(currentUser.get());
            moderateController.load();
            layout.show(cards, MODERATE);
        });
        main.setOnDirections(id -> requestDirections(main, directionsController, id));
        main.setOnLogin(() -> layout.show(cards, LOGIN));
        main.setOnAccount(() -> layout.show(cards, ACCOUNT));

        main.setOnReport(() -> selected(washrooms, main).ifPresentOrElse(
                w -> {
                    status.setWashroomName(w.name());
                    layout.show(cards, STATUS);
                },
                () -> noWashroom(frame)
        ));

        main.setOnBusyness(() -> selected(washrooms, main).ifPresentOrElse(
                w -> {
                    busyness.setLocationName(w.building().name());
                    busynessController.execute(w.id(), w.building().code(), DayOfWeek.from(java.time.LocalDate.now()));
                    layout.show(cards, BUSYNESS);
                },
                () -> noWashroom(frame)
        ));
        main.setFilterController(filterController);
        main.setSortWashroomController(sortWashroomController);

        readReviews.setOnBack(showMain);

        readReviews.setOnWrite(() -> selected(washrooms, main).ifPresentOrElse(
                w -> new WriteReviewDialog(frame, writeReviewModel, writeReviewController, w.id(), w.name(),
                        loggedInModel.getState().loggedIn() ? loggedInModel.getState().username() : "Anonymous",
                        () -> {
                            reviewController.execute(w.id(), currentUser.get());
                            updateWashroomListRating(listModel, w.id(), reviewsModel.getState().rating());
                        }).setVisible(true),
                () -> noWashroom(frame)
        ));
        readReviews.setOnHelpful(id -> {
            voteController.toggle(id, currentUser.get());
            reviewController.execute(reviewsModel.getState().washroomId(), currentUser.get());
        });
        readReviews.setOnReport(id -> {
            // Modal dialog: setVisible blocks until it closes, then refresh the review list (so the
            // button flips to "Reported") and the moderator queue count if a report was filed.
            new ReportReviewDialog(frame, reportController, reportReviewModel, id, currentUser.get()).setVisible(true);
            reviewController.execute(reviewsModel.getState().washroomId(), currentUser.get());
            moderateController.load();
        });
        moderate.setOnBack(showMain);
        moderate.setController(moderateController);

        // Keep the Moderator nav button's reported-review count in sync with the queue: the model is
        // updated on load and after every remove/dismiss, so this listener covers those; load once now
        // for the initial badge.
        moderateModel.addPropertyChangeListener(e -> {
            Runnable update = () -> main.setModeratorReportCount(moderateModel.getState().reportedReviews().size());
            if (SwingUtilities.isEventDispatchThread()) update.run();
            else SwingUtilities.invokeLater(update);
        });

        // Gate the Moderator nav entry on the logged-in user's moderator status (hidden by default).
        loggedInModel.addPropertyChangeListener(e ->
                main.setModerator(loggedInModel.getState().moderator()));
        login.setOnBack(showMain);
        login.setOnSignup(() -> new SignupDialog(frame, signupController).setVisible(true));
        status.setOnCancel(showMain);
        status.setOnSubmit(() -> {
            if (!main.selectedId().isBlank())
                statusController.execute(main.selectedId(), status.busyness(), status.cleanliness(), status.issue(), loggedInModel.getState().loggedIn() ? loggedInModel.getState().username() : null);
        });
        statusModel.addPropertyChangeListener(e -> {
            if (statusModel.getState().success()) {
                refreshHeatmapAsync(displayedWashrooms.get(), reports, main);
            }
        });
        busyness.setOnBack(showMain);
        account.setOnBack(showMain);

        loadMainDataAsync(washrooms, reviews, reports, seedData, moderateController::load,
                main, listModel, displayedWashrooms, originLat, originLng, () -> onLoaded.accept(frame));
        return frame;
    }

    private record MainData(List<Washroom> washrooms, List<MainView.HeatmapData> heatmapData) {
    }
}
