package views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import entity.ReviewSummary;
import entity.Washroom;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.change_password.ChangePasswordController;
import interface_adapter.account.change_username.ChangeUsernameController;
import interface_adapter.account.delete_account.DeleteAccountController;
import interface_adapter.account.personal_plan.PersonalPlanController;
import interface_adapter.busyness.BusynessViewModel;
import interface_adapter.account.IsLoggedInViewModel;
import interface_adapter.directions.MapViewModel;
import interface_adapter.filter.FilterController;
import interface_adapter.filter.FilterViewModel;
import interface_adapter.moderate_reviews.ModerateReviewsController;
import interface_adapter.moderate_reviews.ModerateReviewsViewModel;
import interface_adapter.sort_reviews.SortReviewsController;
import interface_adapter.sort_washrooms.SortWashroomController;
import interface_adapter.status_report.StatusReportViewModel;
import interface_adapter.view_reviews.ReviewsViewModel;
import use_case.busyness.BusynessStatsOutputData;
import use_case.moderate_reviews.ReportedReview;
import use_case.view_reviews.ViewReviewsOutputData;

/** Headless interaction tests for Swing components that do not require a top-level window. */
class SwingInteractionCoverageTest {
    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void accountViewRendersAndDispatchesEveryAccountAction() throws Exception {
        final AccountViewModel model = new AccountViewModel();
        final AtomicInteger usernameCalls = new AtomicInteger();
        final AtomicInteger passwordCalls = new AtomicInteger();
        final AtomicInteger deleteCalls = new AtomicInteger();
        final CountDownLatch planCall = new CountDownLatch(1);
        final CountDownLatch generatedPlanViewed = new CountDownLatch(1);
        final AtomicInteger viewedPlans = new AtomicInteger();
        final AtomicInteger backs = new AtomicInteger();
        final AccountView[] holder = new AccountView[1];

        runOnEdt(() -> {
            final ChangeUsernameController username = new ChangeUsernameController(input -> usernameCalls.incrementAndGet());
            final ChangePasswordController password = new ChangePasswordController(input -> passwordCalls.incrementAndGet());
            final DeleteAccountController delete = new DeleteAccountController(deleteCalls::incrementAndGet);
            final PersonalPlanController plan = new PersonalPlanController(input -> planCall.countDown());
            holder[0] = new AccountView(model, username, password, delete, plan);
            holder[0].setOnViewPlan(value -> {
                if (viewedPlans.incrementAndGet() > 1) {
                    generatedPlanViewed.countDown();
                }
            });
            holder[0].setOnBack(backs::incrementAndGet);
            model.setUsername("alice");
            model.setPersonalPlan("[{\"day\":\"Mon\",\"time\":\"10:00\",\"name\":\"w1\"}]");
            click(holder[0], "View Plan");

            click(holder[0], "Change Username");
            final List<JTextField> usernameFields = textFields(holder[0]);
            usernameFields.getLast().setText("new-alice");
            click(holder[0], "Confirm Username");
            model.setChangeUsernameMessage("Username changed");
            model.setChangeUsernameSuccess(true);

            click(holder[0], "Change Password");
            final List<JPasswordField> passwordFields = passwordFields(holder[0]);
            passwordFields.getFirst().setText("new-password");
            passwordFields.getLast().setText("new-password");
            click(holder[0], "Confirm Password");
            model.setChangePasswordMessage("Password changed");
            model.setChangePasswordSuccess(true);

            click(holder[0], "Delete Account");
            click(holder[0], "Delete Account");
            model.setDeleteAccountMessage("Account deleted");
            model.setDeleteAccountSuccess(true);

            model.setPersonalPlanSuccess(true);
            click(holder[0], "Generate New Plan");
        });
        assertTrue(planCall.await(5, TimeUnit.SECONDS));
        assertTrue(generatedPlanViewed.await(5, TimeUnit.SECONDS));
        runOnEdt(() -> click(holder[0], "<- Back to Map"));
        assertEquals(1, usernameCalls.get());
        assertEquals(1, passwordCalls.get());
        assertEquals(1, deleteCalls.get());
        assertEquals(2, viewedPlans.get());
        assertTrue(backs.get() >= 1);
    }

    @Test
    void reportedReviewsViewRendersDetailsAndModerationActions() throws Exception {
        final ModerateReviewsViewModel model = new ModerateReviewsViewModel();
        final AtomicInteger dismissed = new AtomicInteger();
        final AtomicInteger removed = new AtomicInteger();
        final AtomicInteger backs = new AtomicInteger();
        final ReportedReviewsView[] holder = new ReportedReviewsView[1];
        runOnEdt(() -> {
            holder[0] = new ReportedReviewsView(model);
            holder[0].setModeratorUsername("mod");
            holder[0].setOnBack(backs::incrementAndGet);
            holder[0].setController(new ModerateReviewsController(new use_case.moderate_reviews.ModerateReviewsInputBoundary() {
                @Override public void loadReportedReviews() { }
                @Override public void removeReview(final use_case.moderate_reviews.ModerateReviewsInputData input) {
                    removed.incrementAndGet();
                }
                @Override public void dismissReports(final use_case.moderate_reviews.ModerateReviewsInputData input) {
                    dismissed.incrementAndGet();
                }
            }));
            model.setState(new ModerateReviewsViewModel.State(List.of(), "Nothing pending"));
            model.setState(new ModerateReviewsViewModel.State(List.of(new ReportedReview("r1", "Bahen", "alice",
                LocalDate.of(2026, 8, 1), 4.5, "comment", List.of(new ReportedReview.ReasonCount("Spam", 2)),
                List.of(new ReportedReview.AdditionalDetail("Spam", "more detail")))), ""));
            clickContaining(holder[0], "View Additional Details");
            clickContaining(holder[0], "Hide Additional Details");
            click(holder[0], "Dismiss Reports");
            click(holder[0], "Remove Review");
            click(holder[0], "<- Back to map");
        });
        assertEquals(1, dismissed.get());
        assertEquals(1, removed.get());
        assertEquals(1, backs.get());
    }

    @Test
    void reviewStatusAndChartViewsRenderStateAndDispatchEvents() throws Exception {
        final ReviewsViewModel reviewsModel = new ReviewsViewModel();
        final AtomicInteger helpful = new AtomicInteger();
        final AtomicInteger reports = new AtomicInteger();
        final AtomicInteger writes = new AtomicInteger();
        final AtomicInteger backs = new AtomicInteger();
        final CountDownLatch sorted = new CountDownLatch(1);
        final ReadReviewsView[] reviewsView = new ReadReviewsView[1];
        runOnEdt(() -> {
            reviewsView[0] = new ReadReviewsView(reviewsModel);
            reviewsView[0].setOnHelpful(id -> helpful.incrementAndGet());
            reviewsView[0].setOnReport(id -> reports.incrementAndGet());
            reviewsView[0].setOnWrite(writes::incrementAndGet);
            reviewsView[0].setOnBack(backs::incrementAndGet);
            reviewsView[0].setSortReviewsController(new SortReviewsController(input -> sorted.countDown()));
            final List<ViewReviewsOutputData.ReviewDisplay> displayed = List.of(
                new ViewReviewsOutputData.ReviewDisplay("r1", 4, "helpful", 2, LocalDate.now(), "alice", false, false),
                new ViewReviewsOutputData.ReviewDisplay("r2", 2, "reported", 0, LocalDate.now(), "bob", true, true));
            reviewsModel.setState(new ReviewsViewModel.State("w1", "Main washroom", "Bahen, 2", 3.5, 4, 2, 3, 2,
                displayed, null));
            clickContaining(reviewsView[0], "Helpful");
            click(reviewsView[0], "Report");
            click(reviewsView[0], "+ Write a review");
            click(reviewsView[0], "<- Back to map");
            final List<JComboBox> boxes = components(reviewsView[0], JComboBox.class);
            boxes.getFirst().setSelectedIndex(1);
        });
        assertTrue(sorted.await(5, TimeUnit.SECONDS));
        assertEquals(1, helpful.get());
        assertEquals(1, reports.get());
        assertEquals(1, writes.get());
        assertEquals(1, backs.get());

        final StatusReportViewModel statusModel = new StatusReportViewModel();
        final AtomicInteger submitted = new AtomicInteger();
        final AtomicInteger cancelled = new AtomicInteger();
        runOnEdt(() -> {
            final StatusReportView status = new StatusReportView(statusModel);
            status.setWashroomName("Main washroom");
            status.setOnSubmit(submitted::incrementAndGet);
            status.setOnCancel(cancelled::incrementAndGet);
            statusModel.setState(new StatusReportViewModel.State(false, 0, "Try again"));
            statusModel.setState(new StatusReportViewModel.State(true, 3.2, "Saved"));
            click(status, "Submit status");
            click(status, "Cancel");
            assertEquals(3, status.busyness());
            assertEquals(3, status.cleanliness());
        });
        assertEquals(1, submitted.get());
        assertEquals(1, cancelled.get());

        final BusynessViewModel busynessModel = new BusynessViewModel();
        final AtomicInteger chartBack = new AtomicInteger();
        runOnEdt(() -> {
            final BusynessChartView chart = new BusynessChartView(busynessModel);
            chart.setLocationName("Main washroom");
            chart.setOnBack(chartBack::incrementAndGet);
            chart.setSize(900, 700);
            chart.doLayout();
            final BufferedImage image = new BufferedImage(900, 700, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics = image.createGraphics();
            chart.paint(graphics);
            graphics.dispose();
            final int current = java.time.LocalTime.now().getHour();
            final List<BusynessStatsOutputData.HourBucket> buckets = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                buckets.add(new BusynessStatsOutputData.HourBucket(i == 0 ? current : i, i, 5 - i,
                    "status"));
            }
            busynessModel.setState(new BusynessViewModel.State(buckets, "Status data"));
            chart.doLayout();
            final BufferedImage populated = new BufferedImage(900, 700, BufferedImage.TYPE_INT_ARGB);
            chart.paint(populated.createGraphics());
            click(chart, "<- Back to map");
        });
        assertEquals(1, chartBack.get());
    }

    @Test
    void filterCardsAndMapMarkerListenersCoverSelectionBranches() throws Exception {
        final AtomicInteger filtered = new AtomicInteger();
        final FilterPanel[] filter = new FilterPanel[1];
        runOnEdt(() -> {
            filter[0] = new FilterPanel(null, "BA", filtered::incrementAndGet);
            final List<JSlider> sliders = components(filter[0], JSlider.class);
            sliders.get(0).setValue(2);
            sliders.get(1).setValue(4);
            for (final String label : List.of("Accessible only", "Only washrooms that you've reviewed",
                "Filter to currently selected building", "Only washrooms in your personal plan")) {
                check(filter[0], label);
            }
            final List<JComboBox> boxes = components(filter[0], JComboBox.class);
            for (int index = 0; index < 5; index++) {
                boxes.getFirst().setSelectedIndex(index);
                if (index == 0) {
                    assertEquals(null, filter[0].gender());
                }
                else {
                    assertTrue(filter[0].gender() != null);
                }
            }
            click(filter[0], "Filter");
            assertEquals(2, filter[0].busyness());
            assertEquals(4, filter[0].cleanliness());
            assertTrue(filter[0].accessibleOnly());
            assertTrue(filter[0].ownReviews());
            assertTrue(filter[0].selectedBuilding());
            assertTrue(filter[0].personalPlan());
        });
        assertEquals(1, filtered.get());

        final AtomicReference<String> selected = new AtomicReference<>();
        final MapMarkerClickListener marker = new MapMarkerClickListener(Map.of("w1", new Rectangle(0, 0, 20, 20)),
            () -> selected::set);
        final JComponent source = new javax.swing.JPanel();
        marker.mouseClicked(new MouseEvent(source, MouseEvent.MOUSE_CLICKED, 0, 0, 5, 5, 1, false,
            MouseEvent.BUTTON1));
        marker.mouseClicked(new MouseEvent(source, MouseEvent.MOUSE_CLICKED, 0, 0, 5, 5, 1, false,
            MouseEvent.BUTTON3));
        assertEquals("w1", selected.get());

        final AtomicInteger cardClicks = new AtomicInteger();
        final WashroomListItem item = new WashroomListItem();
        final javax.swing.JPanel card = WashroomCardFactory.create(item.item, item.item.id(), id -> cardClicks.incrementAndGet(),
            id -> cardClicks.incrementAndGet(), id -> cardClicks.incrementAndGet());
        card.dispatchEvent(new MouseEvent(card, MouseEvent.MOUSE_CLICKED, 0, 0, 1, 1, 1, false,
            MouseEvent.BUTTON1));
        click(card, "Reviews");
        click(card, "Directions");
        assertTrue(cardClicks.get() >= 3);
    }

    @Test
    void mainViewRendersMapStateAndNavigationCallbacks() throws Exception {
        final interface_adapter.view_reviews.WashroomListViewModel washrooms =
            new interface_adapter.view_reviews.WashroomListViewModel();
        final MapViewModel route = new MapViewModel();
        final FilterViewModel filter = new FilterViewModel();
        final IsLoggedInViewModel loggedIn = new IsLoggedInViewModel();
        final AtomicInteger callbacks = new AtomicInteger();
        final MainView[] holder = new MainView[1];
        runOnEdt(() -> {
            holder[0] = new MainView(washrooms, route, filter, loggedIn,
                new interface_adapter.logout.LogoutController(callbacks::incrementAndGet),
                new interface_adapter.account.load_account.LoadAccountController(callbacks::incrementAndGet));
            final MainView view = holder[0];
            view.setOnLogin(callbacks::incrementAndGet);
            view.setOnLogout(callbacks::incrementAndGet);
            view.setOnAccount(callbacks::incrementAndGet);
            view.setOnReport(callbacks::incrementAndGet);
            view.setOnBusyness(callbacks::incrementAndGet);
            view.setOnModerator(callbacks::incrementAndGet);
            view.setOnReviews(id -> callbacks.incrementAndGet());
            view.setOnDirections(id -> callbacks.incrementAndGet());
            view.setFilterController(new FilterController(input -> { }));
            view.setSortWashroomController(new SortWashroomController(input -> { }));
            view.setAddressLookup(address -> new entity.GeoPoint(43.66, -79.39));
            view.setAddressLookup(null);
            view.setWashrooms(List.of(new Washroom("w1", "Main", new entity.Building("BA", "Bahen", 43.66,
                -79.39), "2", true, Washroom.Gender.ALL_GENDER, 2, 2, "inside", ReviewSummary.empty())));
            view.setHeatmapData(List.of(new MainView.HeatmapData("w1", 3, 4)));
            view.showRouting();
            view.updateLocation(43.67, -79.4);
            assertEquals(43.67, view.latitude());
            assertEquals(-79.4, view.longitude());

            washrooms.setState(new interface_adapter.view_reviews.WashroomListViewModel.State(
                List.of(new interface_adapter.view_reviews.WashroomListViewModel.Item("w1", "Main", "inside", 4,
                    10, true)), "", "Sort by: Nearest", false));
            click(view, "Reviews");
            click(view, "Directions");
            click(view, "Clear route");
            final List<JComboBox> sortBoxes = components(view, JComboBox.class);
            if (!sortBoxes.isEmpty()) {
                sortBoxes.getLast().setSelectedIndex(1);
            }
            route.setState(new MapViewModel.State(true,
                List.of(new entity.GeoPoint(43.66, -79.39), new entity.GeoPoint(43.67, -79.4)), "1 km", "12",
                ""));
            route.setState(new MapViewModel.State(false, List.of(), "", "", "Route failed"));
            filter.setState(new FilterViewModel.State(true, List.of(), ""));
            view.setModerator(true);
            view.setModeratorReportCount(2);
            loggedIn.setIsLoggedIn(true);
            click(view, "Account");
            click(view, "Report status");
            click(view, "View status");
            clickContaining(view, "Moderator");
            click(view, "Logout");
            loggedIn.setIsLoggedIn(false);
            click(view, "Login");
            view.setModeratorReportCount(0);
            view.setModerator(false);
        });
        assertTrue(callbacks.get() >= 10);
    }

    @Test
    void swingDispatcherRunsOnAndOffTheEventDispatchThread() throws Exception {
        final SwingUiDispatcher dispatcher = new SwingUiDispatcher();
        final CountDownLatch backgroundDispatch = new CountDownLatch(1);
        dispatcher.dispatch(backgroundDispatch::countDown);
        assertTrue(backgroundDispatch.await(5, TimeUnit.SECONDS));
        final AtomicInteger onEdt = new AtomicInteger();
        runOnEdt(() -> dispatcher.dispatch(onEdt::incrementAndGet));
        assertEquals(1, onEdt.get());
    }

    private static void runOnEdt(final Runnable action) throws Exception {
        SwingUtilities.invokeAndWait(action);
    }

    private static void click(final Component root, final String text) {
        for (final JButton button : components(root, JButton.class)) {
            if (button.getText().equals(text)) {
                button.doClick();
                return;
            }
        }
        throw new AssertionError("button not found: " + text);
    }

    private static void clickContaining(final Component root, final String text) {
        for (final JButton button : components(root, JButton.class)) {
            if (button.getText() != null && button.getText().contains(text)) {
                button.doClick();
                return;
            }
        }
        throw new AssertionError("button not found containing: " + text);
    }

    private static void check(final Component root, final String text) {
        for (final JCheckBox checkbox : components(root, JCheckBox.class)) {
            if (checkbox.getText().equals(text)) {
                checkbox.doClick();
                return;
            }
        }
        throw new AssertionError("checkbox not found: " + text);
    }

    private static List<JTextField> textFields(final Component root) {
        final List<JTextField> result = components(root, JTextField.class);
        result.removeIf(field -> field instanceof JPasswordField);
        return result;
    }

    private static List<JPasswordField> passwordFields(final Component root) {
        return components(root, JPasswordField.class);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Component> List<T> components(final Component root, final Class<T> type) {
        final List<T> result = new ArrayList<>();
        if (type.isInstance(root)) {
            result.add((T) root);
        }
        if (root instanceof java.awt.Container container) {
            for (final Component child : container.getComponents()) {
                result.addAll(components(child, type));
            }
        }
        return result;
    }

    private static final class WashroomListItem {
        private final interface_adapter.view_reviews.WashroomListViewModel.Item item =
            new interface_adapter.view_reviews.WashroomListViewModel.Item("w1", "Main washroom", "near elevators",
                4.5, 20, true);
    }
}
