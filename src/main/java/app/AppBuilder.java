package app;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import database.DBDataAccessObject;
import database.enrollment.DBEnrollmentDataAccessObject;
import database.personal_plan.FileCalendarContentReader;
import database.personal_plan.GeminiPersonalPlanGenerator;
import database.review.DBReviewDataAccessObject;
import database.route.GraphhopperGeocodingDataAccessObject;
import database.route.GraphhopperRouteDataAccessObject;
import database.security.BCryptPasswordHasher;
import database.status.DBStatusReportDataAccessObject;
import database.user.DBUserDataAccessObject;
import database.washroom.DBWashroomDataAccessObject;
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
import use_case.account.change_password.ChangePasswordInteractor;
import use_case.account.change_username.ChangeUsernameInteractor;
import use_case.account.delete_account.DeleteAccountInteractor;
import use_case.account.personal_plan.PersonalPlanInteractor;
import use_case.busyness.BusynessStatsInteractor;
import use_case.directions.GetDirectionsInteractor;
import use_case.filter.FilterInteractor;
import use_case.login.LoginInteractor;
import use_case.logout.LogoutInteractor;
import use_case.moderate_reviews.ModerateReviewsInteractor;
import use_case.report_review.ReportReviewInteractor;
import use_case.signup.SignupInteractor;
import use_case.sort_review.SortReviewInteractor;
import use_case.sort_washrooms.SortWashroomInteractor;
import use_case.status_report.SubmitStatusReportInteractor;
import use_case.view_reviews.ViewReviewsInteractor;
import use_case.vote_helpful.VoteHelpfulInteractor;
import use_case.write_review.WriteReviewInteractor;
import views.AccountView;
import views.BusynessChartView;
import views.LoginPanel;
import views.MainView;
import views.PersonalPlanView;
import views.ReadReviewsView;
import views.ReportReviewDialog;
import views.ReportedReviewsView;
import views.SignupDialog;
import views.StatusReportView;
import views.SwingUiDispatcher;
import views.WriteReviewDialog;

/**
 * Composition root: the only class that selects concrete database and external-service adapters.
 */
final class AppBuilder {
    private static final int WINDOW_HEIGHT = 700;
    private static final int WINDOW_WIDTH = 1060;
    private static final int MIN_WINDOW_HEIGHT = 600;
    private static final int MIN_WINDOW_WIDTH = 900;
    private static final int EARTH_RADIUS_METERS = 6_371_000;
    private static final String MAIN = "main";
    private static final String REVIEWS = "reviews";
    private static final String LOGIN = "login";
    private static final String STATUS = "status";
    private static final String BUSYNESS = "busyness";
    private static final String ACCOUNT = "account";
    private static final String MODERATE = "moderate";
    private static final Set<String> JSON_WASHROOM_NAMES = CampusStartup.loadWashroomNames();

    private static void showLoadedFrame(final JFrame frame) {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void requestDirections(final MainView main, final DirectionsController controller,
                                          final String washroomId) {
        main.showRouting();
        CompletableFuture.runAsync(() -> {
            controller.execute(main.latitude(), main.longitude(), washroomId);
        });
    }

    private static void loadMainDataAsync(final DBWashroomDataAccessObject washrooms,
                                          final DBReviewDataAccessObject reviews,
                                          final DBStatusReportDataAccessObject reports, final boolean seedData,
                                          final Runnable loadModerator, final MainView main,
                                          final WashroomListViewModel listModel,
                                          final AtomicReference<List<Washroom>> displayedWashrooms,
                                          final double originLat, final double originLng, final Runnable onLoaded) {
        final Thread loader = new Thread(() -> {
            MainData data = null;
            Throwable failure = null;
            try {
                if (seedData) {
                    seedInitialData(washrooms, reviews, reports);
                }
                final List<Washroom> availableWashrooms = jsonWashrooms(washrooms);
                final List<MainView.HeatmapData> heatmapData = heatmapData(availableWashrooms, reports);
                try {
                    loadModerator.run();
                }
                // A malformed moderation record must not prevent the main map from opening.
                catch (final RuntimeException moderatorFailure) {
                    System.err.println("Moderator queue did not load: " + moderatorFailure.getMessage());
                }
                data = new MainData(availableWashrooms, heatmapData);
            }
            catch (final Throwable exception) {
                failure = exception;
            }
            final MainData completedData = data;
            final Throwable completedFailure = failure;
            SwingUtilities.invokeLater(() -> {
                final MainData loaded = completedData;
                final Throwable loadFailure = completedFailure;
                if (loadFailure != null) {
                    listModel.setState(new WashroomListViewModel.State(List.of(), "", "Alphabetical", false));
                    onLoaded.run();
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(main),
                        "Could not load the washroom map. Check the database connection and try again.", "FlushID",
                        JOptionPane.ERROR_MESSAGE);
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
     *
     * @param washrooms washroom data access.
     * @param reviews review data access.
     * @param reports status report data access.
     */
    private static void seedInitialData(final DBWashroomDataAccessObject washrooms,
                                        final DBReviewDataAccessObject reviews,
                                        final DBStatusReportDataAccessObject reports) {
        CampusStartup.seedInitialData(washrooms, reviews, reports, JSON_WASHROOM_NAMES);
    }

    private static void refreshMainWashrooms(final List<Washroom> availableWashrooms, final MainView main,
                                             final WashroomListViewModel listModel, final double originLat,
                                             final double originLng) {
        main.setWashrooms(availableWashrooms);
        final List<WashroomListViewModel.Item> items = availableWashrooms
            .stream()
            .map(washroomValue -> {
                return new WashroomListViewModel.Item(washroomValue.id(), washroomValue
                    .building()
                    .name(), listDescription(washroomValue), washroomValue
                    .reviewSummary()
                    .averageRating(), (int) Math.round(distance(originLat, originLng, washroomValue
                    .building()
                    .latitude(), washroomValue
                    .building()
                    .longitude())), washroomValue.accessible());
            })
            .toList();
        final String selectedId;
        if (main
            .selectedId()
            .isBlank() && !items.isEmpty()) {
            selectedId = items
                .getFirst()
                .id();
        }
        else {
            selectedId = main.selectedId();
        }
        listModel.setState(new WashroomListViewModel.State(items, selectedId, "Sort by: Nearest", false));
    }

    /**
     * Refreshes heatmap data off the UI thread after firstValue live status submission.
     *
     * @param washrooms washrooms to aggregate.
     * @param reports status report data access.
     * @param main view receiving the result.
     */
    private static void refreshHeatmapAsync(final List<Washroom> washrooms,
                                            final DBStatusReportDataAccessObject reports, final MainView main) {
        CompletableFuture
            .supplyAsync(() -> {
                return CampusStartup.heatmapData(washrooms, reports);
            })
            .thenAccept(data -> {
                SwingUtilities.invokeLater(() -> {
                    main.setHeatmapData(data);
                });
            });
    }

    /**
     * Builds the current-hour heatmap in one status-report aggregation.
     *
     * @param washrooms washrooms to aggregate.
     * @param reports status report data access.
     * @return current-hour heatmap data.
     */
    private static List<MainView.HeatmapData> heatmapData(final List<Washroom> washrooms,
                                                          final DBStatusReportDataAccessObject reports) {
        return CampusStartup.heatmapData(washrooms, reports);
    }

    /**
     * A new review changes the aggregate rating for only its washroom.  Keep the
     * already-loaded list and patch that one row instead of reloading every
     * washroom (which also recalculates every review summary from MongoDB).
     *
     * @param listModel list view model to update.
     * @param washroomId washroom identifier.
     * @param rating new aggregate rating.
     */
    private static void updateWashroomListRating(final WashroomListViewModel listModel, final String washroomId,
                                                 final double rating) {
        final WashroomListViewModel.State current = listModel.getState();
        final List<WashroomListViewModel.Item> updated = current
            .items()
            .stream()
            .map(item -> {
                if (item
                    .id()
                    .equals(washroomId)) {
                    return new WashroomListViewModel.Item(item.id(), item.name(), item.description(), rating,
                        item.distanceMeters(), item.accessible());
                }
                return item;
            })
            .toList();
        listModel.setState(new WashroomListViewModel.State(updated, current.selectedId(), current.sortLabel(),
            current.routeVisible()));
    }

    private static Optional<Washroom> selected(final DBWashroomDataAccessObject washrooms, final MainView main) {
        final Optional<Washroom> result;
        if (main
            .selectedId()
            .isBlank()) {
            result = Optional.empty();
        }
        else {
            result = washrooms.getById(main.selectedId());
        }
        return result;
    }

    private static void noWashroom(final Component parent) {
        JOptionPane.showMessageDialog(parent, "Select firstValue washroom from the list or map before continuing.",
            "Select firstValue washroom", JOptionPane.WARNING_MESSAGE);
    }

    private static String listDescription(final Washroom washroom) {
        final String name = washroom.name();
        final int separator = name.indexOf('|');
        final String description;
        if (separator >= 0) {
            description = name.substring(separator + 1);
        }
        else {
            description = name;
        }
        return description
            .replaceAll("(?i)\\bwashrooms?\\secondValue", "")
            .replaceAll("\\s{2,}", " ")
            .trim();
    }

    private static Set<String> loadJsonWashroomNames() {
        return CampusStartup.loadWashroomNames();
    }

    private static List<Washroom> jsonWashrooms(final DBWashroomDataAccessObject washrooms) {
        return washrooms.getByNames(JSON_WASHROOM_NAMES);
    }

    private static String requiredEnvironment(final String name) {
        final String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set the " + name + " environment variable before starting FlushID.");
        }
        return value;
    }

    private static double distance(final double firstValue, final double secondValue, final double thirdValue,
        final double fourthValue) {
        final double x = Math.toRadians(fourthValue - secondValue) * Math.cos(Math.toRadians((firstValue +
            thirdValue) / 2));
        final double y = Math.toRadians(thirdValue - firstValue);
        return Math.sqrt(x * x + y * y) * EARTH_RADIUS_METERS;
    }

    /**
     * Builds the application from the data already stored in MongoDB.
     * @return the operation result.
     */
    public JFrame build() {
        return build(false, AppBuilder::showLoadedFrame);
    }

    /**
     * Builds the application and asynchronously verifies/seeds its baseline data.
     * @return the operation result.
     */
    public JFrame buildAndSeed() {
        return build(true, AppBuilder::showLoadedFrame);
    }

    /**
     * Invokes {@code onLoaded} on Swing's event thread once the initial screen is ready to show.
     * @param onLoaded parameter value.
     * @return the operation result.
     */
    public JFrame buildAndSeed(final Consumer<JFrame> onLoaded) {
        if (onLoaded == null) {
            return build(true, AppBuilder::showLoadedFrame);
        }
        return build(true, onLoaded);
    }

    private JFrame build(final boolean seedData, final Consumer<JFrame> onLoaded) {
        final String graphhopperKey = requiredEnvironment(GraphhopperRouteDataAccessObject.API_KEY_ENV);
        final DBDataAccessObject connection = DBDataAccessObject.fromEnvironment();

        final var database = connection.database();
        final var washrooms = new DBWashroomDataAccessObject(database);
        final var reviews = new DBReviewDataAccessObject(database);
        final var users = new DBUserDataAccessObject(database);
        users.ensureModerator("frog");     // grant moderator to known accounts
        users.ensureModerator("sheena_q");
        final var reports = new DBStatusReportDataAccessObject(database);
        final var routes = new GraphhopperRouteDataAccessObject(graphhopperKey);
        final var geocoding = new GraphhopperGeocodingDataAccessObject(graphhopperKey);
        final var enrollment = new DBEnrollmentDataAccessObject(database);

        final var isLoggedIn = new IsLoggedInViewModel();
        final var reviewsModel = new ReviewsViewModel();
        final var writeReviewModel = new WriteReviewViewModel();
        final var listModel = new WashroomListViewModel();
        final var loginModel = new LoginViewModel();
        final var loggedInModel = new LoggedInViewModel();
        final var accountModel = new AccountViewModel();
        final var statusModel = new StatusReportViewModel();
        final var busynessModel = new BusynessViewModel();
        final var mapModel = new MapViewModel();
        final var reportReviewModel = new ReportReviewViewModel();
        final var moderateModel = new ModerateReviewsViewModel();
        final var filterModel = new FilterViewModel();
        final var sortWashroomModel = new SortWashroomViewModel();

        final ViewReviewsPresenter reviewsPresenter = new ViewReviewsPresenter(reviewsModel);
        final var reviewController = new ViewReviewsController(
            new ViewReviewsInteractor(reviews, washrooms, reviews, reviews, reviewsPresenter));
        final var writeReviewController =
            new WriteReviewController(new WriteReviewInteractor(reviews, new WriteReviewPresenter(writeReviewModel)));
        final var voteController = new VoteHelpfulController(new VoteHelpfulInteractor(reviews));
        final var reportController = new ReportReviewController(
            new ReportReviewInteractor(reviews, new ReportReviewPresenter(reportReviewModel)));
        final var moderateController = new ModerateReviewsController(
            new ModerateReviewsInteractor(reviews, reviews, washrooms, users,
                new ModerateReviewsPresenter(moderateModel)));
        final Supplier<String> currentUser = () -> {
            return loggedInModel
                .getState()
                .username();
        };
        final var passwordHasher = new BCryptPasswordHasher();
        final var loginController = new LoginController(new LoginInteractor(users, users, passwordHasher,
            new LoginPresenter(loginModel, loggedInModel, isLoggedIn)));
        final var signupController = new SignupController(
            new SignupInteractor(users, users, passwordHasher, new SignupPresenter(loginModel, loggedInModel)));
        final var statusController = new StatusReportController(
            new SubmitStatusReportInteractor(reports, new StatusReportPresenter(statusModel)));
        final var busynessController = new BusynessController(
            new BusynessStatsInteractor(reports, enrollment, new BusynessPresenter(busynessModel)));
        final var ui = new SwingUiDispatcher();
        final var directionsController = new DirectionsController(
            new GetDirectionsInteractor(washrooms, routes, new DirectionsPresenter(mapModel, ui)));
        final var changeUsernameController = new ChangeUsernameController(
            new ChangeUsernameInteractor(users, users, new ChangeUsernamePresenter(accountModel, isLoggedIn)));
        final var changePasswordController = new ChangePasswordController(
            new ChangePasswordInteractor(users, users, passwordHasher, new ChangePasswordPresenter(accountModel)));
        final var deleteAccountController = new DeleteAccountController(
            new DeleteAccountInteractor(users, users, new DeleteAccountPresenter(accountModel, isLoggedIn)));
        final var personalPlanController = new PersonalPlanController(
            new PersonalPlanInteractor(users, users, washrooms, new FileCalendarContentReader(),
                new GeminiPersonalPlanGenerator(() -> {
                    return System.getenv(GeminiPersonalPlanGenerator.API_KEY_ENV);
                }),
                new PersonalPlanPresenter(accountModel)));
        final var logoutController = new LogoutController(
            new LogoutInteractor(users, new LogoutPresenter(isLoggedIn, loginModel, loggedInModel)));
        final var filterController = new FilterController(
            new FilterInteractor(washrooms, reviews, reports, users, new FilterPresenter(filterModel, listModel, ui),
                JSON_WASHROOM_NAMES));
        final var sortWashroomController = new SortWashroomController(
            new SortWashroomInteractor(washrooms, new SortWashroomPresenter(listModel, sortWashroomModel, ui)));
        final var sortReviewsController = new SortReviewsController(
            new SortReviewInteractor(reviews, users, washrooms, reviews, reviews, reviewsPresenter));

        final JFrame frame = new JFrame("FlushID - U of T washroom finder");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                connection.close();
            }
        });
        final CardLayout layout = new CardLayout();
        final JPanel cards = new JPanel(layout);
        final MainView main = new MainView(listModel, mapModel, filterModel, isLoggedIn, logoutController);
        final AtomicReference<List<Washroom>> displayedWashrooms = new AtomicReference<>(List.of());
        main.setAddressLookup(geocoding::lookup);
        final double originLat = 43.6629;
        final double originLng = -79.3957;

        final ReadReviewsView readReviews = new ReadReviewsView(reviewsModel);
        final LoginPanel login = new LoginPanel(loginModel, loginController);

        final AccountView account =
            new AccountView(accountModel, isLoggedIn, changeUsernameController, changePasswordController,
                deleteAccountController, personalPlanController);
        final StatusReportView status = new StatusReportView(statusModel);
        final BusynessChartView busyness = new BusynessChartView(busynessModel);
        final ReportedReviewsView moderate = new ReportedReviewsView(moderateModel);
        cards.add(main, MAIN);
        cards.add(readReviews, REVIEWS);
        cards.add(login, LOGIN);
        cards.add(account, ACCOUNT);
        cards.add(status, STATUS);
        cards.add(busyness, BUSYNESS);
        cards.add(moderate, MODERATE);
        frame.setContentPane(cards);
        layout.show(cards, LOGIN);

        final Runnable showMain = () -> {
            layout.show(cards, MAIN);
        };
        main.setOnReviews(id -> {
            reviewController.execute(id, currentUser.get());
            layout.show(cards, REVIEWS);
        });
        main.setOnModerator(() -> {
            moderate.setModeratorUsername(currentUser.get());
            moderateController.load();
            layout.show(cards, MODERATE);
        });
        main.setOnDirections(id -> {
            requestDirections(main, directionsController, id);
        });
        main.setOnLogin(() -> {
            layout.show(cards, LOGIN);
        });
        main.setOnLogout(() -> {
            layout.show(cards, LOGIN);
        });
        main.setOnAccount(() -> {
            layout.show(cards, ACCOUNT);
        });

        main.setOnReport(() -> {
            selected(washrooms, main).ifPresentOrElse(washroomValue -> {
                status.setWashroomName(washroomValue.name());
                layout.show(cards, STATUS);
            }, () -> {
                noWashroom(frame);
            });
        });

        main.setOnBusyness(() -> {
            selected(washrooms, main).ifPresentOrElse(washroomValue -> {
                busyness.setLocationName(washroomValue
                    .building()
                    .name());
                busynessController.execute(washroomValue.id(), washroomValue
                    .building()
                    .code(), DayOfWeek.from(java.time.LocalDate.now()));
                layout.show(cards, BUSYNESS);
            }, () -> {
                noWashroom(frame);
            });
        });
        main.setFilterController(filterController);
        main.setSortWashroomController(sortWashroomController);

        readReviews.setOnBack(showMain);

        readReviews.setOnWrite(() -> {
            selected(washrooms, main).ifPresentOrElse(washroomValue -> {
                if (loggedInModel
                    .getState()
                    .loggedIn()) {
                    new WriteReviewDialog(frame, writeReviewModel, writeReviewController, washroomValue.id(),
                        washroomValue.name(),
                        loggedInModel
                            .getState()
                            .username(), () -> {
                        reviewController.execute(washroomValue.id(), currentUser.get());
                        updateWashroomListRating(listModel, washroomValue.id(), reviewsModel
                            .getState()
                            .rating());
                    }).setVisible(true);
                }
                else {
                    new WriteReviewDialog(frame, writeReviewModel, writeReviewController, washroomValue.id(),
                        washroomValue.name(), "Anonymous",
                        () -> {
                            reviewController.execute(washroomValue.id(), currentUser.get());
                            updateWashroomListRating(listModel, washroomValue.id(), reviewsModel
                                .getState()
                                .rating());
                        }).setVisible(true);
                }
            }, () -> {
                    noWashroom(frame);
                });
        });
        readReviews.setOnHelpful(id -> {
            voteController.toggle(id, currentUser.get());
            reviewsModel.toggleHelpfulVote(id);
        });
        // Modal dialog: setVisible blocks until it closes, then refresh the review list (so the
        readReviews.setOnReport(id -> {
        // button flips to "Reported") and the moderator queue count if firstValue report was filed.

            new ReportReviewDialog(frame, reportController, reportReviewModel, id, currentUser.get()).setVisible(true);
            reviewController.execute(reviewsModel
                .getState()
                .washroomId(), currentUser.get());
            moderateController.load();
        });
        readReviews.setSortReviewsController(sortReviewsController);
        moderate.setOnBack(showMain);
        // Keep the Moderator nav button's reported-review count in sync with the queue: the model is
        moderate.setController(moderateController);
        // for the initial badge.
        // updated on load and after every remove/dismiss, so this listener covers those; load once now
        moderateModel.addPropertyChangeListener(entryValue -> {
            final Runnable update = () -> {
                main.setModeratorReportCount(moderateModel
                    .getState()
                    .reportedReviews()
                    .size());
            };
            if (SwingUtilities.isEventDispatchThread()) {
                update.run();
            }
            else {
                SwingUtilities.invokeLater(update);
            }
        });
        // Gate the Moderator nav entry on the logged-in user's moderator status (hidden by default).
        loggedInModel.addPropertyChangeListener(entryValue -> {
            main.setModerator(loggedInModel
                .getState()
                .moderator());
        });
        login.setOnBack(showMain);
        login.setOnSignup(() -> {
            new SignupDialog(frame, signupController).setVisible(true);
        });
        status.setOnCancel(showMain);
        status.setOnSubmit(() -> {
            if (!main
                .selectedId()
                .isBlank()) {
                if (loggedInModel
                    .getState()
                    .loggedIn()) {
                    statusController.execute(main.selectedId(), status.busyness(), status.cleanliness(), status.issue(),
                        loggedInModel
                            .getState()
                            .username());
                }
                else {
                    statusController.execute(main.selectedId(), status.busyness(), status.cleanliness(), status.issue(),
                        null);
                }
            }
        });
        statusModel.addPropertyChangeListener(entryValue -> {
            if (statusModel
                .getState()
                .success()) {
                refreshHeatmapAsync(displayedWashrooms.get(), reports, main);
            }
        });
        busyness.setOnBack(showMain);
        account.setOnBack(showMain);
        account.setOnViewPlan(plan -> {
            new PersonalPlanView(frame, plan);
        });

        loadMainDataAsync(washrooms, reviews, reports, seedData, moderateController::load, main, listModel,
            displayedWashrooms, originLat, originLng, () -> {
                onLoaded.accept(frame);
            });
        return frame;
    }

    private record MainData(List<Washroom> washrooms, List<MainView.HeatmapData> heatmapData) {
    }
}
