package views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import database.user.InMemoryUserDataAccessObject;
import entity.GeoPoint;
import entity.Washroom;
import interface_adapter.account.IsLoggedInState;
import interface_adapter.account.IsLoggedInViewModel;
import interface_adapter.directions.MapViewModel;
import interface_adapter.filter.FilterController;
import interface_adapter.filter.FilterViewModel;
import interface_adapter.login.LoggedInViewModel;
import interface_adapter.login.LoginViewModel;
import interface_adapter.logout.LogoutController;
import interface_adapter.logout.LogoutPresenter;
import interface_adapter.sort_washrooms.SortWashroomController;
import interface_adapter.view_reviews.WashroomListViewModel;
import use_case.logout.LogoutInteractor;

public final class MainView extends JPanel {
    private static final Color MAP_LOW = Theme.COLORBLIND_BLUE;
    private static final Color MAP_HIGH = Theme.COLORBLIND_ORANGE;
    /**
     * Okabe-Ito endpoints keep map values distinguishable with colour-vision deficiencies.
     */
    private final LogoutController logoutController;
    private final CardLayout buttonsLayout = new CardLayout();
    private final JPanel buttonsPanel = new JPanel(buttonsLayout);
    /**
     * Okabe-Ito endpoints keep map values distinguishable with colour-vision deficiencies.
     */
    private final JPanel list = new JPanel();
    private final JLabel routeLabel = Theme.label("Select a washroom to explore", 13, Theme.MUTED);
    private final JLabel heatmapLegend = Theme.label("", 11, Theme.MUTED);
    private final CampusMapPanel map = new CampusMapPanel();
    private final Map<String, JPanel> cardsByWashroomId = new HashMap<>();
    private IsLoggedInViewModel isLoggedIn = new IsLoggedInViewModel();
    private JButton moderatorNav;
    private JButton busynessHeatmap, cleanlinessHeatmap;
    private boolean busynessHeatmapVisible, cleanlinessHeatmapVisible;
    private String selectedId = "";
    private List<WashroomListViewModel.Item> renderedItems = List.of();
    private FilterController filterController;
    private SortWashroomController sortWashroomController;

    private Consumer<String> onReviews = id -> {
    };
    private Consumer<String> onDirections = id -> {
    };

    private Runnable onLogin = () -> {
    }, onReport = () -> {
    }, onBusyness = () -> {
    }, onAccount = () -> {
    }, onModerator = () -> {
    }, onLogout = () -> {
    };

    private Function<String, GeoPoint> addressLookup = address -> {
        throw new IllegalStateException("Address search is unavailable.");
    };
    private double latitude = 43.6629, longitude = -79.3957;

    /**
     * Retained for callers that do not provide filtering controls.
     */
    public MainView(final WashroomListViewModel washrooms,
                    // TODO: why is this still here its being a pain
                    final MapViewModel route) {
        this(washrooms, route, new FilterViewModel(), new IsLoggedInViewModel(), new LogoutController(
            new LogoutInteractor(new InMemoryUserDataAccessObject(),
                new LogoutPresenter(new IsLoggedInViewModel(), new LoginViewModel(), new LoggedInViewModel()))));
    }

    public MainView(final WashroomListViewModel washrooms, final MapViewModel route, final FilterViewModel filter,
                    final IsLoggedInViewModel isLoggedIn, final LogoutController logoutController) {
        this.isLoggedIn = isLoggedIn;
        this.logoutController = logoutController;
        isLoggedIn
            .getState()
            .addPropertyChangeListener(e -> {
                render(isLoggedIn.getState());
            });
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        final JComponent loggedIn = headerLoggedIn();
        final JComponent loggedOut = headerLoggedOut();
        buttonsPanel.add(loggedOut, "loggedOut");
        buttonsPanel.add(loggedIn, "loggedIn");
        add(buttonsPanel, BorderLayout.NORTH);
        final JSplitPane content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar(washrooms), mapArea());
        content.setDividerLocation(290);
        content.setDividerSize(8);
        content.setContinuousLayout(true);
        add(content, BorderLayout.CENTER);
        map.setOnWashroomSelected(this::selectWashroom);
        washrooms.addPropertyChangeListener(e -> {
            renderList(washrooms
                .getState()
                .items());
        });
        route.addPropertyChangeListener(e -> {
            final MapViewModel.State s = route.getState();
            if (s.success()) {

                routeLabel.setText("<html><b>" + s.distance() + "</b> · about " + s.duration()
                    + " walk · live GraphHopper route</html>");
                map.setRoute(s.points());
            }
            else {
                routeLabel.setText(s.message());
                map.clearRoute();
            }
        });
        filter.addPropertyChangeListener(e -> {
            final FilterViewModel.State s = filter.getState();
            if (!s.success()) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), s.message());
            }
            else {
                map.setWashrooms(s.washrooms());
            }
        });
    }

    private JComponent headerLoggedIn() {
        final JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.PAPER);
        p.setBorder(Theme.pad(10, 18, 10, 18));
        final JLabel brand = Theme.label("FlushID", 20, Theme.BLUE);
        brand.setFont(brand
            .getFont()
            .deriveFont(Font.BOLD));
        p.add(brand, BorderLayout.WEST);
        final JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nav.setOpaque(false);
        moderatorNav = nav("Moderator", () -> {
            onModerator.run();
        });
        // hidden until a moderator logs in
        moderatorNav.setVisible(false);
        for (final JButton b : new JButton[] {nav("Account", () -> {
            onAccount.run();
        }),
            nav("Report status", () -> {
                onReport.run();
            }), nav("View status", () -> {
            onBusyness.run();
        }), moderatorNav}) {
            nav.add(b);
        }
        final JButton logoutButton = Theme.button("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {

                onLogout.run();
                logoutController.execute();

            }
        });
        nav.add(logoutButton);
        p.add(nav, BorderLayout.EAST);
        return p;
    }

    private JComponent headerLoggedOut() {
        final JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.PAPER);
        p.setBorder(Theme.pad(10, 18, 10, 18));
        final JLabel brand = Theme.label("FlushID", 20, Theme.BLUE);
        brand.setFont(brand
            .getFont()
            .deriveFont(Font.BOLD));
        p.add(brand, BorderLayout.WEST);
        final JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nav.setOpaque(false);
        for (final JButton b : new JButton[] {nav("Report status", () -> {
            onReport.run();
        }),
            nav("View status", () -> {
                onBusyness.run();
            }), nav("Login", () -> {
            onLogin.run();
        })}) {
            nav.add(b);
        }
        p.add(nav, BorderLayout.EAST);
        return p;
    }

    private JButton nav(final String text, final Runnable action) {
        final JButton b = Theme.button(text);
        b.addActionListener(e -> {
            action.run();
        });
        return b;
    }

    private JComponent sidebar(final WashroomListViewModel washrooms) {
        final JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setPreferredSize(new Dimension(290, 0));
        p.setBackground(Theme.PAPER);
        p.setBorder(Theme.pad(14, 18, 14, 12));
        final JPanel controls = new JPanel(new GridLayout(3, 2, 8, 8));
        controls.setOpaque(false);
        final JButton location = Theme.button("Location");
        final JButton filters = Theme.button("Filters");
        final JButton clear = Theme.button("Clear Filters");
        controls.add(location);
        controls.add(filters);
        controls.add(new JLabel());
        controls.add(clear);
        controls.add(new JLabel("Sort by:"));
        final WashroomSortDropdownControl washroomSortDropdownControl = new WashroomSortDropdownControl();
        controls.add(washroomSortDropdownControl);
        washroomSortDropdownControl.addActionListener(e -> {
            final ArrayList<String> washroomIdList = new ArrayList<String>();
            washroomIdList.addAll(washrooms
                .getState()
                .items()
                .stream()
                .map(washroom -> {
                    return washroom.id();
                })
                .toList());
            sortWashroomController.execute(washroomSortDropdownControl
                .getSelectedItem()
                .toString(), washroomIdList, latitude, longitude);
        });
        controls.add(washroomSortDropdownControl);
        p.add(controls, BorderLayout.NORTH);

        final MapClicker mapClicker = new MapClicker(map);
        location.addActionListener(
            e -> {
                new LocationInputDialog(SwingUtilities.getWindowAncestor(this), addressLookup, (lat, lng) -> {
                    latitude = lat;
                    longitude = lng;
                    map.setOrigin(new GeoPoint(lat, lng));
                    routeLabel.setText("Location updated — choose directions");
                }, latitude, longitude, mapClicker).setVisible(true);
            });
        filters.addActionListener(
            e -> {
                new FilterView(SwingUtilities.getWindowAncestor(this), "Filter", selectedId(), filterController,
                    latitude, longitude).setVisible(true);
            });
        clear.addActionListener(
            e -> {
                filterController.execute(5, 1, false, false, false, false, selectedId(), null, latitude, longitude);
            });
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Theme.PAPER);
        final JScrollPane scroll = new JScrollPane(list);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll
            .getVerticalScrollBar()
            .setUnitIncrement(32);
        scroll
            .getVerticalScrollBar()
            .setBlockIncrement(192);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JComponent mapArea() {
        final JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Theme.PAPER);
        p.setBorder(Theme.pad(14, 12, 14, 18));
        final JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.CREAM);
        bar.setBorder(Theme.pad(10, 12, 10, 12));
        final JPanel mapStatus = new JPanel();
        mapStatus.setOpaque(false);
        mapStatus.setLayout(new BoxLayout(mapStatus, BoxLayout.Y_AXIS));
        mapStatus.add(routeLabel);
        heatmapLegend.setVisible(false);
        heatmapLegend.setAlignmentX(Component.LEFT_ALIGNMENT);
        mapStatus.add(heatmapLegend);
        bar.add(mapStatus, BorderLayout.CENTER);
        final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        busynessHeatmap = Theme.button("Busyness heatmap");
        cleanlinessHeatmap = Theme.button("Cleanliness heatmap");
        busynessHeatmap.addActionListener(e -> {
            busynessHeatmapVisible = !busynessHeatmapVisible;
            updateHeatmapControls();
        });
        cleanlinessHeatmap.addActionListener(e -> {
            cleanlinessHeatmapVisible = !cleanlinessHeatmapVisible;
            updateHeatmapControls();
        });
        final JButton clear = Theme.button("Clear route");
        clear.addActionListener(e -> {
            map.clearRoute();
            routeLabel.setText("Select a washroom to explore");
        });
        actions.add(busynessHeatmap);
        actions.add(cleanlinessHeatmap);
        actions.add(clear);
        bar.add(actions, BorderLayout.EAST);
        p.add(bar, BorderLayout.NORTH);
        p.add(map);
        return p;
    }

    private void updateHeatmapControls() {
        if (busynessHeatmapVisible) {
            busynessHeatmap.setText("Busyness heatmap" + " (On)");
        }
        else {
            busynessHeatmap.setText("Busyness heatmap" + "");
        }
        if (cleanlinessHeatmapVisible) {
            cleanlinessHeatmap.setText("Cleanliness heatmap" + " (On)");
        }
        else {
            cleanlinessHeatmap.setText("Cleanliness heatmap" + "");
        }
        if (busynessHeatmapVisible && cleanlinessHeatmapVisible) {
            heatmapLegend.setText(
                "Heatmap: busyness outer glow · cleanliness inner glow · blue = lower · orange = higher · gray = no "
                    + "data");
        }
        else if (busynessHeatmapVisible) {
            heatmapLegend.setText("Busyness heatmap: blue = less busy · orange = more busy · gray = no data");
        }
        else if (cleanlinessHeatmapVisible) {
            heatmapLegend.setText("Cleanliness heatmap: blue = lower · orange = higher · gray = no data");
        }
        heatmapLegend.setVisible(busynessHeatmapVisible || cleanlinessHeatmapVisible);
        map.setHeatmapVisibility(busynessHeatmapVisible, cleanlinessHeatmapVisible);
    }

    private void renderList(final List<WashroomListViewModel.Item> items) {
        renderedItems = List.copyOf(items);
        list.removeAll();
        cardsByWashroomId.clear();
        if (!selectedId.isBlank() && items
            .stream()
            .noneMatch(item -> {
                return item
                    .id()
                    .equals(selectedId);
            })) {
            selectedId = "";
        }
        map.setSelectedWashroom(selectedId);
        for (final var item : items) {
            final JPanel card = new JPanel(new BorderLayout(4, 4));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 124));
            if (item
                .id()
                .equals(selectedId)) {
                card.setBackground(Theme.PALE_BLUE);
            }
            else {
                card.setBackground(Theme.PAPER);
            }
            if (item
                .id()
                .equals(selectedId)) {
                card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BLUE),
                    Theme.pad(10, 10, 10, 10)));
            }
            else {
                card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE),
                    Theme.pad(10, 10, 10, 10)));
            }
            final JLabel name;
            if (item
                .id()
                .equals(selectedId)) {
                name = Theme.label(item.name(), 14, Theme.BLUE);
            }
            else {
                name = Theme.label(item.name(), 14, Theme.INK);
            }
            name.setFont(name
                .getFont()
                .deriveFont(Font.BOLD));
            card.add(name, BorderLayout.NORTH);
            final JLabel description = Theme.label(item.description(), 12, Theme.INK);
            description.setFont(description
                .getFont()
                .deriveFont(Font.BOLD));
            description.setAlignmentX(Component.LEFT_ALIGNMENT);
            final JLabel details =
                Theme.label(String.format("★ %.1f · %d m away", item.rating(), item.distanceMeters()), 12, Theme.MUTED);
            final JPanel information = new JPanel();
            information.setLayout(new BoxLayout(information, BoxLayout.Y_AXIS));
            information.setOpaque(false);
            information.add(description);
            information.add(Box.createVerticalStrut(4));
            details.setAlignmentX(Component.LEFT_ALIGNMENT);
            information.add(details);
            card.add(information, BorderLayout.CENTER);
            final JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            actions.setOpaque(false);
            final JButton reviews = Theme.button("Reviews");
            final JButton directions = Theme.button("Directions");
            reviews.setPreferredSize(new Dimension(78, 28));
            directions.setPreferredSize(new Dimension(94, 28));
            final MouseAdapter select = new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent event) {
                    selectWashroom(item.id());
                }
            };
            card.addMouseListener(select);
            name.addMouseListener(select);
            information.addMouseListener(select);
            description.addMouseListener(select);
            details.addMouseListener(select);
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            name.setCursor(card.getCursor());
            information.setCursor(card.getCursor());
            description.setCursor(card.getCursor());
            details.setCursor(card.getCursor());
            reviews.addActionListener(e -> {
                selectWashroom(item.id());
                onReviews.accept(item.id());
            });
            directions.addActionListener(e -> {
                selectWashroom(item.id());
                onDirections.accept(item.id());
            });
            actions.add(reviews);
            actions.add(directions);
            card.add(actions, BorderLayout.SOUTH);
            cardsByWashroomId.put(item.id(), card);
            list.add(card);
            list.add(Box.createVerticalStrut(10));
        }
        list.revalidate();
        list.repaint();
    }

    private void selectWashroom(final String id) {
        if (id == null || id.isBlank() || id.equals(selectedId)) {
            return;
        }
        selectedId = id;
        renderList(renderedItems);
        scrollSelectedCardIntoView();
    }

    private void scrollSelectedCardIntoView() {
        final JPanel selectedCard = cardsByWashroomId.get(selectedId);
        if (selectedCard == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            selectedCard.scrollRectToVisible(new Rectangle(0, 0, selectedCard.getWidth(), selectedCard.getHeight()));
        });
    }

    public void setWashrooms(final List<Washroom> washrooms) {
        map.setWashrooms(washrooms);
    }

    /**
     * Supplies the latest reported values used by the optional map heatmap layers.
     */
    public void setHeatmapData(final List<HeatmapData> values) {
        map.setHeatmapData(values);
    }

    public void setAddressLookup(final Function<String, GeoPoint> lookup) {
        if (lookup == null) {
            addressLookup = address -> {
                throw new IllegalStateException("Address search is unavailable.");
            };
        }
        else {
            addressLookup = lookup;
        }
    }

    public void setOnReviews(final Consumer<String> c) {
        onReviews = c;
    }

    public void setOnDirections(final Consumer<String> c) {
        onDirections = c;
    }

    public void setOnLogin(final Runnable r) {
        onLogin = r;
    }

    public void setOnLogout(final Runnable r) {
        onLogout = r;
    }

    public void setOnAccount(final Runnable r) {
        onAccount = r;
    }

    public void setOnReport(final Runnable r) {
        onReport = r;
    }

    public void setOnBusyness(final Runnable r) {
        onBusyness = r;
    }

    public void setOnModerator(final Runnable r) {
        onModerator = r;
    }

    public void setFilterController(final FilterController f) {
        filterController = f;
    }

    public void setSortWashroomController(final SortWashroomController controller) {
        sortWashroomController = controller;
    }

    /**
     * Shows the Moderator nav entry only for a user with moderator privileges.
     */
    public void setModerator(final boolean isModerator) {
        if (moderatorNav != null) {
            moderatorNav.setVisible(isModerator);
        }
    }

    /**
     * Reflects the number of reported reviews awaiting moderation on the Moderator nav button:
     * appends the count and accents the label (berry, bold) when there is a queue, plain otherwise.
     */
    public void setModeratorReportCount(final int n) {
        if (moderatorNav == null) {
            return;
        }
        if (n > 0) {
            moderatorNav.setText("Moderator (" + n + ")");
        }
        else {
            moderatorNav.setText("Moderator");
        }
        if (n > 0) {
            moderatorNav.setForeground(Theme.BERRY);
        }
        else {
            moderatorNav.setForeground(Color.BLACK);
        }
        if (n > 0) {
            moderatorNav.setFont(moderatorNav
                .getFont()
                .deriveFont(Font.BOLD));
        }
        else {
            moderatorNav.setFont(moderatorNav
                .getFont()
                .deriveFont(Font.PLAIN));
        }
    }

    public void showRouting() {
        routeLabel.setText("Requesting a live walking route from GraphHopper…");
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    public String selectedId() {
        return selectedId;
    }

    private void render(final IsLoggedInState state) {

        if (state.getIsLoggedIn()) {
            buttonsLayout.show(buttonsPanel, "loggedIn");
        }
        else {
            buttonsLayout.show(buttonsPanel, "loggedOut");
        }

    }

    /**
     * A NaN value means that no recent report is available for that measurement.
     */
    public record HeatmapData(String washroomId, double busyness, double cleanliness) {
        public HeatmapData {
            if (washroomId == null || washroomId.isBlank()) {
                throw new IllegalArgumentException("washroomId is required");
            }
        }
    }

    final class CampusMapPanel extends JPanel {
        private final JXMapViewer viewer;
        private final Map<String, Rectangle> markerHitTargets = new HashMap<>();
        private List<GeoPoint> route = List.of();
        private List<Washroom> washrooms = List.of();
        private Map<String, HeatmapData> heatmapData = Map.of();
        private String selectedWashroomId = "";
        private boolean showBusynessHeatmap, showCleanlinessHeatmap;
        private Consumer<String> onWashroomSelected = id -> {
        };
        private GeoPoint origin = new GeoPoint(43.6629, -79.3957);

        CampusMapPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Theme.LINE));
            if (GraphicsEnvironment.isHeadless()) {
                viewer = null;
                add(Theme.label("Interactive OpenStreetMap", 13, Theme.MUTED), BorderLayout.CENTER);
                return;
            }

            final TileFactoryInfo tileInfo = new OSMTileFactoryInfo();
            final File cacheDirectory = new File(System.getProperty("user.home"), ".flushid/map-cache");
            final DefaultTileFactory tileFactory = new DefaultTileFactory(tileInfo);
            tileFactory.setUserAgent("FlushID/1.0 (U of T student map demo)");
            tileFactory.setLocalCache(new FileBasedLocalCache(cacheDirectory, false));
            tileFactory.setThreadPoolSize(4);

            viewer = new JXMapViewer();
            viewer.setTileFactory(tileFactory);
            viewer.setAddressLocation(toPosition(origin));
            viewer.setZoom(3);
            viewer.setRestrictOutsidePanning(true);
            viewer.setHorizontalWrapped(false);
            viewer.setOverlayPainter(this::paintOverlay);

            final MouseInputListener pan = new PanMouseInputListener(viewer);
            viewer.addMouseListener(pan);
            viewer.addMouseMotionListener(pan);
            viewer.addMouseListener(new CenterMapListener(viewer));
            viewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(viewer));
            viewer.addKeyListener(new PanKeyListener(viewer));
            viewer.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent event) {
                    if (!SwingUtilities.isLeftMouseButton(event)) {
                        return;
                    }
                    for (final Map.Entry<String, Rectangle> target : markerHitTargets.entrySet()) {
                        if (target
                            .getValue()
                            .contains(event.getPoint())) {
                            onWashroomSelected.accept(target.getKey());
                            return;
                        }
                    }
                }
            });
            viewer.setFocusable(true);

            final JLabel attribution =
                Theme.label("Drag to pan · mouse wheel to zoom · © OpenStreetMap contributors · routes by GraphHopper",
                    10, Theme.MUTED);
            attribution.setBorder(Theme.pad(4, 8, 4, 8));
            add(viewer, BorderLayout.CENTER);
            add(attribution, BorderLayout.SOUTH);
        }

        /**
         * Keeps each glow approximately the same real-world size as the map zoom changes.
         */
        private static int scaledHeatRadius(final JXMapViewer map, final int baseRadius) {
            final double zoomFactor = Math.pow(2, 3 - map.getZoom());
            return (int) Math.round(Math.max(24, Math.min(180, baseRadius * zoomFactor)));
        }

        private static Color heatColor(final double value) {
            if (Double.isNaN(value)) {
                return Theme.NO_DATA;
            }
            final double progress = Math.max(0, Math.min(1, (value - 1) / 4));
            return new Color((int) Math.round(MAP_LOW.getRed() + (MAP_HIGH.getRed() - MAP_LOW.getRed()) * progress),
                (int) Math.round(MAP_LOW.getGreen() + (MAP_HIGH.getGreen() - MAP_LOW.getGreen()) * progress),
                (int) Math.round(MAP_LOW.getBlue() + (MAP_HIGH.getBlue() - MAP_LOW.getBlue()) * progress));
        }

        private static Color withAlpha(final Color color, final int alpha) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        }

        private static GeoPosition toPosition(final GeoPoint point) {
            return new GeoPosition(point.latitude(), point.longitude());
        }

        void setWashrooms(final List<Washroom> values) {
            washrooms = List.copyOf(values);
            if (viewer != null) {
                viewer.repaint();
                SwingUtilities.invokeLater(this::fitCampus);
            }
        }

        void setHeatmapData(final List<HeatmapData> values) {
            final Map<String, HeatmapData> byWashroomId = new HashMap<>();
            for (final HeatmapData value : values) {
                byWashroomId.put(value.washroomId(), value);
            }
            heatmapData = Map.copyOf(byWashroomId);
            if (viewer != null) {
                viewer.repaint();
            }
        }

        void setHeatmapVisibility(final boolean busynessVisible, final boolean cleanlinessVisible) {
            showBusynessHeatmap = busynessVisible;
            showCleanlinessHeatmap = cleanlinessVisible;
            if (viewer != null) {
                viewer.repaint();
            }
        }

        void setSelectedWashroom(final String id) {
            if (id == null) {
                selectedWashroomId = "";
            }
            else {
                selectedWashroomId = id;
            }
            if (viewer != null) {
                viewer.repaint();
            }
        }

        void setOnWashroomSelected(final Consumer<String> action) {
            if (action == null) {
                onWashroomSelected = id -> {
                };
            }
            else {
                onWashroomSelected = action;
            }
        }

        void setOrigin(final GeoPoint value) {
            origin = value;
            if (viewer != null) {
                viewer.repaint();
            }
        }

        void setRoute(final List<GeoPoint> points) {
            route = List.copyOf(points);
            if (viewer != null) {
                viewer.repaint();
                SwingUtilities.invokeLater(this::fitRoute);
            }
        }

        void clearRoute() {
            route = List.of();
            if (viewer != null) {
                viewer.repaint();
            }
        }

        private void paintOverlay(final Graphics2D graphics, final JXMapViewer map, final int width, final int height) {
            final Graphics2D canvas = (Graphics2D) graphics.create();
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final Rectangle viewport = map.getViewportBounds();
            canvas.translate(-viewport.x, -viewport.y);
            markerHitTargets.clear();
            final Map<GeoPoint, List<Washroom>> washroomsByLocation = washroomsByLocation();
            drawHeatmaps(canvas, map, washroomsByLocation);
            drawRoute(canvas, map);
            drawPoint(canvas, map, viewport, origin, "You", MAP_LOW, "", "", false);
            for (final List<Washroom> washroomsAtLocation : washroomsByLocation.values()) {
                final Washroom representative = washroomsAtLocation.getFirst();
                final boolean selected = washroomsAtLocation
                    .stream()
                    .anyMatch(washroom -> {
                        return washroom
                            .id()
                            .equals(selectedWashroomId);
                    });
                if (selected) {
                    drawPoint(canvas, map, viewport, new GeoPoint(representative
                        .building()
                        .latitude(), representative
                        .building()
                        .longitude()), representative
                        .building()
                        .name(), MAP_LOW, representative
                        .building()
                        .code(), representative.id(), selected);
                }
                else {
                    drawPoint(canvas, map, viewport, new GeoPoint(representative
                        .building()
                        .latitude(), representative
                        .building()
                        .longitude()), representative
                        .building()
                        .name(), MAP_HIGH, representative
                        .building()
                        .code(), representative.id(), selected);
                }
            }
            canvas.dispose();
        }

        private Map<GeoPoint, List<Washroom>> washroomsByLocation() {
            final Map<GeoPoint, List<Washroom>> values = new LinkedHashMap<>();
            for (final Washroom washroom : washrooms) {
                final GeoPoint location = new GeoPoint(washroom
                    .building()
                    .latitude(), washroom
                    .building()
                    .longitude());
                values
                    .computeIfAbsent(location, ignored -> {
                        return new ArrayList<>();
                    })
                    .add(washroom);
            }
            return values;
        }

        private void drawHeatmaps(final Graphics2D canvas, final JXMapViewer map,
                                  final Map<GeoPoint, List<Washroom>> washroomsByLocation) {
            if (!showBusynessHeatmap && !showCleanlinessHeatmap) {
                return;
            }
            for (final Map.Entry<GeoPoint, List<Washroom>> entry : washroomsByLocation.entrySet()) {
                final Point2D point = map
                    .getTileFactory()
                    .geoToPixel(toPosition(entry.getKey()), map.getZoom());
                if (showBusynessHeatmap) {
                    if (showCleanlinessHeatmap) {
                        drawHeat(canvas, point, averageReportedValue(entry.getValue(), true),
                            scaledHeatRadius(map, 86));
                    }
                    else {
                        drawHeat(canvas, point, averageReportedValue(entry.getValue(), true),
                            scaledHeatRadius(map, 70));
                    }
                }
                if (showCleanlinessHeatmap) {
                    if (showBusynessHeatmap) {
                        drawHeat(canvas, point, averageReportedValue(entry.getValue(), false),
                            scaledHeatRadius(map, 54));
                    }
                    else {
                        drawHeat(canvas, point, averageReportedValue(entry.getValue(), false),
                            scaledHeatRadius(map, 70));
                    }
                }
            }
        }

        private double averageReportedValue(final List<Washroom> washroomsAtLocation, final boolean busyness) {
            return washroomsAtLocation
                .stream()
                .map(washroom -> {
                    return heatmapData.get(washroom.id());
                })
                .filter(java.util.Objects::nonNull)
                .mapToDouble(value -> {
                    if (busyness) {
                        return value.busyness();
                    }
                    return value.cleanliness();
                })
                .filter(value -> {
                    return !Double.isNaN(value);
                })
                .average()
                .orElse(Double.NaN);
        }

        private void drawHeat(final Graphics2D canvas, final Point2D point, final double value, final int radius) {
            final Color color = heatColor(value);
            final float[] stops = {0f, .55f, 1f};
            final Color[] colors = {withAlpha(color, 150), withAlpha(color, 72), withAlpha(color, 0)};
            canvas.setPaint(new RadialGradientPaint((float) point.getX(), (float) point.getY(), radius, stops, colors));
            canvas.fillOval((int) point.getX() - radius, (int) point.getY() - radius, radius * 2, radius * 2);
        }

        private void drawRoute(final Graphics2D canvas, final JXMapViewer map) {
            if (route.size() < 2) {
                return;
            }
            final Path2D path = new Path2D.Double();
            for (int index = 0; index < route.size(); index++) {
                final Point2D point = map
                    .getTileFactory()
                    .geoToPixel(toPosition(route.get(index)), map.getZoom());
                if (index == 0) {
                    path.moveTo(point.getX(), point.getY());
                }
                else {
                    path.lineTo(point.getX(), point.getY());
                }
            }
            canvas.setColor(new Color(255, 255, 255, 210));
            canvas.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            canvas.draw(path);
            canvas.setColor(MAP_LOW);
            canvas.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            canvas.draw(path);
        }

        private void drawPoint(final Graphics2D canvas, final JXMapViewer map, final Rectangle viewport,
                               final GeoPoint geoPoint, final String label, final Color color, final String code,
                               final String washroomId, final boolean selected) {
            final Point2D point = map
                .getTileFactory()
                .geoToPixel(toPosition(geoPoint), map.getZoom());
            final int x = (int) point.getX();
            final int y = (int) point.getY();
            if (selected) {
                canvas.setColor(Color.WHITE);
                canvas.fillOval(x - 14, y - 14, 28, 28);
            }
            canvas.setColor(color);
            final int radius;
            if (selected) {
                radius = 11;
            }
            else {
                radius = 10;
            }
            canvas.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            if (!code.isBlank()) {
                canvas.setColor(Color.WHITE);
                canvas.setFont(canvas
                    .getFont()
                    .deriveFont(Font.BOLD, 8f));
                canvas.drawString(code, x - canvas
                    .getFontMetrics()
                    .stringWidth(code) / 2, y + 3);
            }
            if (selected && !label.isBlank()) {
                canvas.setFont(canvas
                    .getFont()
                    .deriveFont(Font.BOLD, 11f));
                final int labelWidth = canvas
                    .getFontMetrics()
                    .stringWidth(label);
                canvas.setColor(new Color(255, 255, 255, 225));
                canvas.fillRoundRect(x + 13, y - 20, labelWidth + 10, 18, 8, 8);
                canvas.setColor(Theme.INK);
                canvas.drawString(label, x + 18, y - 7);
            }
            if (!washroomId.isBlank()) {
                markerHitTargets.put(washroomId, new Rectangle(x - viewport.x - 16, y - viewport.y - 24, 32, 32));
            }
        }

        private void fitCampus() {
            if (viewer == null || washrooms.isEmpty() || viewer.getWidth() == 0) {
                return;
            }
            final Set<GeoPosition> positions = new HashSet<>();
            positions.add(toPosition(origin));
            for (final Washroom washroom : washrooms) {
                positions.add(new GeoPosition(washroom
                    .building()
                    .latitude(), washroom
                    .building()
                    .longitude()));
            }
            viewer.zoomToBestFit(positions, .72);
        }

        private void fitRoute() {
            if (viewer == null || route.isEmpty() || viewer.getWidth() == 0) {
                return;
            }
            final Set<GeoPosition> positions = new HashSet<>();
            for (final GeoPoint point : route) {
                positions.add(toPosition(point));
            }
            viewer.zoomToBestFit(positions, .82);
        }

        @Override
        public void addMouseListener(final MouseListener m) {
            System.out.println("added mouselistener");
            viewer.addMouseListener(m);
        }

        @Override
        public void removeMouseListener(final MouseListener m) {
            viewer.removeMouseListener(m);
        }

        public GeoPosition convertPointToGeoPosition(final Point2D pt) {
            return viewer.convertPointToGeoPosition(pt);
        }
    }
}
