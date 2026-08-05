import interface_adapter.busyness.BusynessViewModel;
import interface_adapter.directions.MapViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginViewModel;
import interface_adapter.recommend.RecommendationViewModel;
import interface_adapter.status_report.StatusReportViewModel;
import interface_adapter.view_reviews.ReviewsViewModel;
import interface_adapter.view_reviews.WashroomListViewModel;
import entity.Building;
import entity.ReviewSummary;
import entity.Washroom;
import view.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import use_case.busyness.BusynessStatsOutputData;

final class UiSmokeTest {
    static void run() {
        System.setProperty("java.awt.headless", "true");
        try {
            SwingUtilities.invokeAndWait(() -> {
                var list = new WashroomListViewModel();
                var reviews = new ReviewsViewModel();
                var map = new MapViewModel();
                var busyness = new BusynessViewModel();
                MainView main = new MainView(list, map);
                main.setWashrooms(List.of(new Washroom("w1", "Bahen Centre Washroom",
                        new Building("BA", "Bahen Centre", 43.6596, -79.3972), "Main floor", false, Washroom.Gender.ALL_GENDER, 0, 0, "Main floor", ReviewSummary.empty())));
                List<JPanel> views = List.of(main, new ReadReviewsView(reviews),
                        new LoginPanel(new LoginViewModel(), new LoginController(input -> {
                        })),
                        new RecommendationView(new RecommendationViewModel()), new StatusReportView(new StatusReportViewModel()),
                        new BusynessChartView(busyness), new FilterPanel());
                list.setState(new WashroomListViewModel.State(List.of(new WashroomListViewModel.Item("w1", "Test washroom", 4.5, 200, true)), "w1", "Nearest", false));
                busyness.setState(new BusynessViewModel.State(List.of(new BusynessStatsOutputData.HourBucket(8, 0, "no data")), "No data yet"));
                for (JPanel view : views) {
                    view.setSize(900, 600);
                    layoutTree(view);
                    assertButtonContrast(view);
                    BufferedImage image = new BufferedImage(900, 600, BufferedImage.TYPE_INT_ARGB);
                    view.paint(image.getGraphics());
                    TestSupport.check(view.getComponentCount() > 0, view.getClass().getSimpleName() + " should contain UI controls");
                }
            });
        } catch (Exception e) {
            throw new AssertionError("UI smoke rendering failed", e);
        }
    }

    private static void assertButtonContrast(Container container) {
        for (Component child : container.getComponents()) {
            if (child instanceof JButton button && button.getText() != null && !button.getText().isBlank()) {
                TestSupport.check(Color.BLACK.equals(button.getForeground()), button.getText() + " should use black text");
                TestSupport.check(Color.WHITE.equals(button.getBackground()), button.getText() + " should use a white background");
            }
            if (child instanceof Container nested) assertButtonContrast(nested);
        }
    }

    private static void layoutTree(Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) if (child instanceof Container nested) layoutTree(nested);
    }
}
