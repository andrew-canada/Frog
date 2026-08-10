package views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import entity.GeoPoint;
import interface_adapter.filter.FilterController;
import interface_adapter.sort_washrooms.SortWashroomController;
import interface_adapter.view_reviews.WashroomListViewModel;

final class MainSidebar {
    private MainSidebar() {
    }

    static JPanel create(final MainView owner, final WashroomListViewModel washrooms, final MainView.CampusMapPanel map,
                         final Supplier<FilterController> filterController,
                         final Supplier<SortWashroomController> sortController,
                         final Supplier<Function<String, GeoPoint>> addressLookup) {
        final JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(MainView.CONTENT_DIVIDER_LOCATION, 0));
        panel.setBackground(Theme.PAPER);
        panel.setBorder(Theme.pad(MainView.MARKER_RADIUS, MainView.MAP_PADDING_VERTICAL, MainView.MARKER_RADIUS,
            MainView.TEXT_FONT_SIZE));
        final JPanel controls = new JPanel(new GridLayout(3, 2, 8, 8));
        controls.setOpaque(false);
        final JButton location = Theme.button("Location");
        final JButton filters = Theme.button("Filters");
        final JButton clear = Theme.button("Clear Filters");
        controls.add(location);
        controls.add(filters);
        controls.add(Theme.label("", MainView.BODY_FONT_SIZE, Theme.PAPER));
        controls.add(clear);
        controls.add(Theme.label("Sort by:", MainView.BODY_FONT_SIZE, Theme.INK));
        final WashroomSortDropdownControl sortDropdown = new WashroomSortDropdownControl();
        controls.add(sortDropdown);
        sortDropdown.addActionListener(event -> {
            sortWashrooms(owner, washrooms, sortController.get(), sortDropdown);
        });
        panel.add(controls, BorderLayout.NORTH);

        final MapClicker mapClicker = new MapClicker(map);
        location.addActionListener(event -> openLocation(owner, addressLookup.get(), mapClicker));
        filters.addActionListener(event -> openFilters(owner, filterController.get()));
        clear.addActionListener(event -> {
            filterController.get().execute(MainView.STANDARD_STROKE_WIDTH, 1, false, false, false, false,
                owner.selectedId(), null, owner.latitude(), owner.longitude());
        });
        final JScrollPane scroll = new JScrollPane(owner.washroomList());
        scroll.getVerticalScrollBar().setUnitIncrement(MainView.STANDARD_SIZE);
        scroll.getVerticalScrollBar().setBlockIncrement(MainView.SCROLL_BLOCK_INCREMENT);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private static void sortWashrooms(final MainView owner, final WashroomListViewModel washrooms,
                                      final SortWashroomController sortController,
                                      final WashroomSortDropdownControl sortDropdown) {
        sortController.execute(sortDropdown.getSelectedItem().toString(), washrooms.getState().items().stream()
            .map(WashroomListViewModel.Item::id)
            .toList(), owner.latitude(), owner.longitude());
    }

    private static void openLocation(final MainView owner, final Function<String, GeoPoint> addressLookup,
                                     final MapClicker mapClicker) {
        new LocationInputDialog(SwingUtilities.getWindowAncestor(owner), addressLookup, owner::updateLocation,
            owner.latitude(), owner.longitude(), mapClicker).setVisible(true);
    }

    private static void openFilters(final MainView owner, final FilterController filterController) {
        new FilterView(SwingUtilities.getWindowAncestor(owner), "Filter", owner.selectedId(), filterController,
            owner.latitude(), owner.longitude()).setVisible(true);
    }
}
