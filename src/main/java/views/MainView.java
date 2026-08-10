package views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import entity.GeoPoint;
import entity.Washroom;
import interface_adapter.account.IsLoggedInViewModel;
import interface_adapter.directions.MapViewModel;
import interface_adapter.filter.FilterController;
import interface_adapter.filter.FilterViewModel;
import interface_adapter.logout.LogoutController;
import interface_adapter.sort_washrooms.SortWashroomController;
import interface_adapter.view_reviews.WashroomListViewModel;

public final class MainView extends JPanel {
    static final int STANDARD_SIZE = 32;
    static final int MAP_PADDING_VERTICAL = 18;
    static final int BODY_FONT_SIZE = 13;
    static final int MARKER_RADIUS = 14;
    static final int STANDARD_STROKE_WIDTH = 5;
    static final int SCROLL_BLOCK_INCREMENT = 192;
    static final int CONTENT_DIVIDER_LOCATION = 290;
    static final int TEXT_FONT_SIZE = 12;
    private static final String FIELD_BUSYNESS_HEATMAP = "Busyness heatmap";
    private static final String FIELD_CLEANLINESS_HEATMAP = "Cleanliness heatmap";
    private static final double WIDE_MAP_FIT_RATIO = .82;
    private static final double TIGHT_MAP_FIT_RATIO = .72;
    private static final int MAP_PADDING_LARGE = 24;
    private static final int MAP_PADDING_MEDIUM = 16;
    private static final int MARKER_LABEL_OFFSET = 7;
    private static final int MAP_PADDING_SMALL = 8;
    private static final int CARD_PADDING = 10;
    private static final int MARKER_LABEL_HEIGHT = 20;
    private static final int MARKER_LABEL_ALPHA = 225;
    private static final int COLOR_CHANNEL_MAX = 255;
    private static final float MARKER_FONT_SIZE = 11f;
    private static final int MAP_ZOOM_LEVEL = 3;
    private static final float MARKER_CODE_FONT_SIZE = 8f;
    private static final int SMALL_MARKER_RADIUS = 11;
    private static final int MARKER_DIAMETER = 28;
    private static final int MARKER_STROKE_WIDTH = 9;
    private static final int MARKER_ALPHA = 210;
    private static final int HEAT_RADIUS_MEDIUM = 70;
    private static final int HEAT_RADIUS_SMALL = 54;
    private static final int HEAT_RADIUS_LARGE = 86;
    private static final int HEAT_RADIUS_MAX = 180;
    private static final int SMALL_GAP = 4;
    private static final double MAP_ORIGIN_LONGITUDE = -79.3957;
    private static final double MAP_ORIGIN_LATITUDE = 43.6629;
    private static final int DIRECTIONS_BUTTON_WIDTH = 94;
    private static final int REVIEWS_BUTTON_WIDTH = 78;
    private static final int CARD_MAX_HEIGHT = 124;
    private static final Color MAP_LOW = Theme.COLORBLIND_BLUE;
    private static final Color MAP_HIGH = Theme.COLORBLIND_ORANGE;
    /**
     * Okabe-Ito endpoints keep map values distinguishable with colour-vision deficiencies.
     */
    private final Runnable logoutAction;
    private final CardLayout buttonsLayout = new CardLayout();
    private final JPanel buttonsPanel = new JPanel(buttonsLayout);
    /**
     * Okabe-Ito endpoints keep map values distinguishable with colour-vision deficiencies.
     */
    private final JPanel list = new JPanel();
    private final JLabel routeLabel = Theme.label("Select a washroom to explore", 13, Theme.MUTED);
    private final JLabel heatmapLegend = Theme.label("", 11, Theme.MUTED);
    private final CampusMapPanel map = new CampusMapPanel();
    private final Map<String, JPanel> cardsByWashroomId = new LinkedHashMap<>();
    private IsLoggedInViewModel isLoggedIn = new IsLoggedInViewModel();
    private JButton moderatorNav;
    private JButton busynessHeatmap;
    private JButton cleanlinessHeatmap;
    private boolean busynessHeatmapVisible;
    private boolean cleanlinessHeatmapVisible;
    private String selectedId = "";
    private List<WashroomListViewModel.Item> renderedItems = List.of();
    private FilterController filterController;
    private SortWashroomController sortWashroomController;

    private Consumer<String> onReviews = id -> {
    };
    private Consumer<String> onDirections = id -> {
    };

    private Runnable onLogin = () -> {
    };
    private Runnable onReport = () -> {
    };
    private Runnable onBusyness = () -> {
    };
    private Runnable onAccount = () -> {
    };
    private Runnable onModerator = () -> {
    };
    private Runnable onLogout = () -> {
    };

    private Function<String, GeoPoint> addressLookup = address -> {
        throw new IllegalStateException("Address search is unavailable.");
    };
    private double latitude = MAP_ORIGIN_LATITUDE;
    private double longitude = MAP_ORIGIN_LONGITUDE;

    /**
     * Retained for callers that do not provide optional controls.
     *
     * @param washrooms washroom view model.
     * @param route route view model.
     */
    public MainView(final WashroomListViewModel washrooms, final MapViewModel route) {
        this(washrooms, route, new FilterViewModel(), new IsLoggedInViewModel(), () -> {
        });
    }

    public MainView(final WashroomListViewModel washrooms, final MapViewModel route, final FilterViewModel filter,
                    final IsLoggedInViewModel isLoggedIn, final LogoutController logoutController) {
        this(washrooms, route, filter, isLoggedIn, logoutController::execute);
    }

    private MainView(final WashroomListViewModel washrooms, final MapViewModel route, final FilterViewModel filter,
                     final IsLoggedInViewModel isLoggedIn, final Runnable logoutAction) {
        this.isLoggedIn = isLoggedIn;
        this.logoutAction = logoutAction;
        isLoggedIn.getState().addPropertyChangeListener(entryValue -> {
            if (isLoggedIn.getState().getIsLoggedIn()) {
                buttonsLayout.show(buttonsPanel, "loggedIn");
            }
            else {
                buttonsLayout.show(buttonsPanel, "loggedOut");
            }
        });
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        final JPanel loggedIn = headerLoggedIn();
        final JPanel loggedOut = headerLoggedOut();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Theme.PAPER);
        buttonsPanel.add(loggedOut, "loggedOut");
        buttonsPanel.add(loggedIn, "loggedIn");
        add(buttonsPanel, BorderLayout.NORTH);
        final JPanel content = new JPanel(new BorderLayout());
        content.add(MainSidebar.create(this, washrooms, map, () -> filterController, () -> sortWashroomController,
            () -> addressLookup), BorderLayout.WEST);
        content.add(mapArea(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
        map.setOnWashroomSelected(this::selectWashroom);
        washrooms.addPropertyChangeListener(entryValue -> {
            renderList(washrooms
                .getState()
                .items());
        });
        route.addPropertyChangeListener(entryValue -> updateRoute(route.getState()));
        filter.addPropertyChangeListener(event -> {
            final FilterViewModel.State s = filter.getState();
            if (!s.success()) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), s.message());
            }
            else {
                map.setWashrooms(s.washrooms());
            }
        });
    }

    private JPanel headerLoggedIn() {
        final JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.PAPER);
        p.setBorder(Theme.pad(CARD_PADDING, MAP_PADDING_VERTICAL, CARD_PADDING, MAP_PADDING_VERTICAL));
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
        nav.add(nav("Account", () -> {
            onAccount.run();
        }));
        nav.add(nav("Report status", () -> {
            onReport.run();
        }));
        nav.add(nav("View status", () -> {
            onBusyness.run();
        }));
        nav.add(moderatorNav);
        final JButton logoutButton = Theme.button("Logout");
        logoutButton.addActionListener(entryValue -> {
            onLogout.run();
            logoutAction.run();
        });
        nav.add(logoutButton);
        p.add(nav, BorderLayout.EAST);
        return p;
    }

    private JPanel headerLoggedOut() {
        final JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.PAPER);
        p.setBorder(Theme.pad(CARD_PADDING, MAP_PADDING_VERTICAL, CARD_PADDING, MAP_PADDING_VERTICAL));
        final JLabel brand = Theme.label("FlushID", 20, Theme.BLUE);
        brand.setFont(brand
            .getFont()
            .deriveFont(Font.BOLD));
        p.add(brand, BorderLayout.WEST);
        final JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nav.setOpaque(false);
        nav.add(nav("Report status", () -> {
            onReport.run();
        }));
        nav.add(nav("View status", () -> {
            onBusyness.run();
        }));
        nav.add(nav("Login", () -> {
            onLogin.run();
        }));
        p.add(nav, BorderLayout.EAST);
        return p;
    }

    private JButton nav(final String text, final Runnable action) {
        final JButton b = Theme.button(text);
        b.addActionListener(entryValue -> {
            action.run();
        });
        return b;
    }

    private JPanel mapArea() {
        final JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Theme.PAPER);
        p.setBorder(Theme.pad(MARKER_RADIUS, TEXT_FONT_SIZE, MARKER_RADIUS, MAP_PADDING_VERTICAL));
        final JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.CREAM);
        bar.setBorder(Theme.pad(CARD_PADDING, TEXT_FONT_SIZE, CARD_PADDING, TEXT_FONT_SIZE));
        final JPanel mapStatus = new JPanel();
        mapStatus.setOpaque(false);
        mapStatus.setLayout(new BoxLayout(mapStatus, BoxLayout.Y_AXIS));
        mapStatus.add(routeLabel);
        heatmapLegend.setVisible(false);
        heatmapLegend.setAlignmentX(0.0f);
        mapStatus.add(heatmapLegend);
        bar.add(mapStatus, BorderLayout.CENTER);
        final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        busynessHeatmap = Theme.button(FIELD_BUSYNESS_HEATMAP);
        cleanlinessHeatmap = Theme.button(FIELD_CLEANLINESS_HEATMAP);
        busynessHeatmap.addActionListener(entryValue -> {
            busynessHeatmapVisible = !busynessHeatmapVisible;
            updateHeatmapControls();
        });
        cleanlinessHeatmap.addActionListener(entryValue -> {
            cleanlinessHeatmapVisible = !cleanlinessHeatmapVisible;
            updateHeatmapControls();
        });
        final JButton clear = Theme.button("Clear route");
        clear.addActionListener(entryValue -> {
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
            busynessHeatmap.setText(FIELD_BUSYNESS_HEATMAP + " (On)");
        }
        else {
            busynessHeatmap.setText(FIELD_BUSYNESS_HEATMAP + "");
        }
        if (cleanlinessHeatmapVisible) {
            cleanlinessHeatmap.setText(FIELD_CLEANLINESS_HEATMAP + " (On)");
        }
        else {
            cleanlinessHeatmap.setText(FIELD_CLEANLINESS_HEATMAP + "");
        }
        if (busynessHeatmapVisible && cleanlinessHeatmapVisible) {
            heatmapLegend.setText(
                "Heatmap: busyness outer glow  -  cleanliness inner glow  -  "
                    + "blue = lower  -  orange = higher  -  gray = no data");
        }
        else if (busynessHeatmapVisible) {
            heatmapLegend.setText("Busyness heatmap: blue = less busy  -  orange = more busy  -  gray = no data");
        }
        else if (cleanlinessHeatmapVisible) {
            heatmapLegend.setText("Cleanliness heatmap: blue = lower  -  orange = higher  -  gray = no data");
        }
        heatmapLegend.setVisible(busynessHeatmapVisible || cleanlinessHeatmapVisible);
        map.setHeatmapVisibility(busynessHeatmapVisible, cleanlinessHeatmapVisible);
    }

    private void updateRoute(final MapViewModel.State state) {
        if (state.success()) {
            routeLabel.setText("<html><b>" + state.distance() + "</b>  -  about " + state.duration()
                + " walk  -  live GraphHopper route</html>");
            map.setRoute(state.points());
        }
        else {
            routeLabel.setText(state.message());
            map.clearRoute();
        }
    }

    void updateLocation(final double newLatitude, final double newLongitude) {
        latitude = newLatitude;
        longitude = newLongitude;
        map.setOrigin(new GeoPoint(newLatitude, newLongitude));
        routeLabel.setText("Location updated - choose directions");
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
        for (final WashroomListViewModel.Item item : items) {
            final JPanel card = WashroomCardFactory.create(item, selectedId, this::selectWashroom,
                id -> onReviews.accept(id), id -> onDirections.accept(id));
            cardsByWashroomId.put(item.id(), card);
            list.add(card);
            list.add(Box.createVerticalStrut(CARD_PADDING));
        }
        list.revalidate();
        list.repaint();
    }

    private void selectWashroom(final String id) {
        if (id != null && !id.isBlank() && !id.equals(selectedId)) {
            selectedId = id;
            renderList(renderedItems);
            scrollSelectedCardIntoView();
        }
    }

    private void scrollSelectedCardIntoView() {
        final JPanel selectedCard = cardsByWashroomId.get(selectedId);
        if (selectedCard != null) {
            SwingUtilities.invokeLater(() -> {
                selectedCard.scrollRectToVisible(selectedCard.getBounds());
            });
        }
    }

    /**
     * Performs this operation.
     *
     * @param washrooms parameter value.
     */
    public void setWashrooms(final List<Washroom> washrooms) {
        map.setWashrooms(washrooms);
    }

    /**
     * Supplies the latest reported values used by the optional map heatmap layers.
     * @param values parameter value.
     */
    public void setHeatmapData(final List<HeatmapData> values) {
        map.setHeatmapData(values);
    }

    /**
     * Performs this operation.
     *
     * @param lookup parameter value.
     */
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

    public void setOnReviews(final Consumer<String> thirdValue) {
        onReviews = thirdValue;
    }

    public void setOnDirections(final Consumer<String> thirdValue) {
        onDirections = thirdValue;
    }

    public void setOnLogin(final Runnable reviewValue) {
        onLogin = reviewValue;
    }

    public void setOnLogout(final Runnable reviewValue) {
        onLogout = reviewValue;
    }

    public void setOnAccount(final Runnable reviewValue) {
        onAccount = reviewValue;
    }

    public void setOnReport(final Runnable reviewValue) {
        onReport = reviewValue;
    }

    public void setOnBusyness(final Runnable reviewValue) {
        onBusyness = reviewValue;
    }

    public void setOnModerator(final Runnable reviewValue) {
        onModerator = reviewValue;
    }

    public void setFilterController(final FilterController parameterValue) {
        filterController = parameterValue;
    }

    public void setSortWashroomController(final SortWashroomController controller) {
        sortWashroomController = controller;
    }

    /**
     * Shows the Moderator nav entry only for a user with moderator privileges.
     * @param isModerator parameter value.
     */
    public void setModerator(final boolean isModerator) {
        if (moderatorNav != null) {
            moderatorNav.setVisible(isModerator);
        }
    }

    /**
     * Reflects the number of reported reviews awaiting moderation on the Moderator nav button:
     * appends the count and accents the label (berry, bold) when there is a queue, plain otherwise.
     * @param numberValue parameter value.
     */
    public void setModeratorReportCount(final int numberValue) {
        if (moderatorNav != null) {
            final boolean hasReports = numberValue > 0;
            final String label;
            final Color color;
            final int fontStyle;
            if (hasReports) {
                label = "Moderator (" + numberValue + ")";
                color = Theme.BERRY;
                fontStyle = Font.BOLD;
            }
            else {
                label = "Moderator";
                color = Color.BLACK;
                fontStyle = Font.PLAIN;
            }
            moderatorNav.setText(label);
            moderatorNav.setForeground(color);
            moderatorNav.setFont(moderatorNav.getFont().deriveFont(fontStyle));
        }
    }

    /**
     * Performs this operation.
     */
    public void showRouting() {
        routeLabel.setText("Requesting a live walking route from GraphHopper...");
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public double latitude() {
        return latitude;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public double longitude() {
        return longitude;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public String selectedId() {
        return selectedId;
    }

    JPanel washroomList() {
        return list;
    }

    /**
     * A NaN value means that no recent report is available for that measurement.
     * @param busyness parameter value.
     * @param cleanliness parameter value.
     * @param washroomId parameter value.
     */
    public record HeatmapData(String washroomId, double busyness, double cleanliness) {
        public HeatmapData {
            if (washroomId == null || washroomId.isBlank()) {
                throw new IllegalArgumentException("washroomId is required");
            }
        }
    }

    final class CampusMapPanel extends JPanel {
        private JXMapViewer viewer;
        private final Map<String, Rectangle> markerHitTargets = new LinkedHashMap<>();
        private List<GeoPoint> route = List.of();
        private List<Washroom> washrooms = List.of();
        private Map<String, HeatmapData> heatmapData = Map.of();
        private String selectedWashroomId = "";
        private boolean showBusynessHeatmap;
        private boolean showCleanlinessHeatmap;
        private Consumer<String> onWashroomSelected = id -> {
        };
        private GeoPoint origin = new GeoPoint(MAP_ORIGIN_LATITUDE, MAP_ORIGIN_LONGITUDE);

        CampusMapPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Theme.LINE));
            if (GraphicsEnvironment.isHeadless()) {
                viewer = null;
                add(Theme.label("Interactive OpenStreetMap", BODY_FONT_SIZE, Theme.MUTED), BorderLayout.CENTER);
            }
            else {
                initializeViewer();
            }
        }

        private void initializeViewer() {
            viewer = MapViewerSetup.create(origin);
            viewer.setOverlayPainter(this::paintOverlay);
            viewer.addMouseListener(new MapMarkerClickListener(markerHitTargets, () -> onWashroomSelected));
            viewer.setFocusable(true);
            final JLabel attribution = Theme.label(
                "Drag to pan  -  mouse wheel to zoom  -  (c) OpenStreetMap contributors  -  routes by GraphHopper",
                10, Theme.MUTED);
            attribution.setBorder(Theme.pad(SMALL_GAP, MAP_PADDING_SMALL, SMALL_GAP, MAP_PADDING_SMALL));
            add(viewer, BorderLayout.CENTER);
            add(attribution, BorderLayout.SOUTH);
        }

        /**
         * Keeps each glow approximately the same real-world size as the map zoom changes.
         * @param baseRadius parameter value.
         * @param mapViewer map view.
         * @return the operation result.
         */
        private static int scaledHeatRadius(final JXMapViewer mapViewer, final int baseRadius) {
            final double zoomFactor = Math.pow(2, 3 - mapViewer.getZoom());
            return (int) Math.round(Math.max(MAP_PADDING_LARGE, Math.min(HEAT_RADIUS_MAX, baseRadius * zoomFactor)));
        }

        private static Color heatColor(final double value) {
            final Color result;
            if (Double.isNaN(value)) {
                result = Theme.NO_DATA;
            }
            else {
                final double progress = Math.max(0, Math.min(1, (value - 1) / 4));
                result = new Color((int) Math.round(MAP_LOW.getRed()
                    + (MAP_HIGH.getRed() - MAP_LOW.getRed()) * progress),
                    (int) Math.round(MAP_LOW.getGreen()
                        + (MAP_HIGH.getGreen() - MAP_LOW.getGreen()) * progress),
                    (int) Math.round(MAP_LOW.getBlue()
                        + (MAP_HIGH.getBlue() - MAP_LOW.getBlue()) * progress));
            }
            return result;
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
            final Map<String, HeatmapData> byWashroomId = new LinkedHashMap<>();
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

        private void paintOverlay(final Graphics2D graphics, final JXMapViewer mapViewer, final int width,
                                  final int height) {
            final Graphics2D canvas = (Graphics2D) graphics.create();
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final Rectangle viewport = mapViewer.getViewportBounds();
            canvas.translate(-viewport.x, -viewport.y);
            markerHitTargets.clear();
            final Map<GeoPoint, List<Washroom>> washroomsByLocation = washroomsByLocation();
            drawHeatmaps(canvas, mapViewer, washroomsByLocation);
            drawRoute(canvas, mapViewer);
            drawPoint(canvas, mapViewer, viewport, new PointData(origin, "You", MAP_LOW, "", "", false));
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
                    drawPoint(canvas, mapViewer, viewport, new PointData(new GeoPoint(representative
                        .building()
                        .latitude(), representative
                        .building()
                        .longitude()), representative
                        .building()
                        .name(), MAP_LOW, representative
                        .building()
                        .code(), representative.id(), selected));
                }
                else {
                    drawPoint(canvas, mapViewer, viewport, new PointData(new GeoPoint(representative
                        .building()
                        .latitude(), representative
                        .building()
                        .longitude()), representative
                        .building()
                        .name(), MAP_HIGH, representative
                        .building()
                        .code(), representative.id(), selected));
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

        private void drawHeatmaps(final Graphics2D canvas, final JXMapViewer mapViewer,
                                  final Map<GeoPoint, List<Washroom>> washroomsByLocation) {
            if (showBusynessHeatmap || showCleanlinessHeatmap) {
                for (final Map.Entry<GeoPoint, List<Washroom>> entry : washroomsByLocation.entrySet()) {
                    final Point2D point = mapViewer
                        .getTileFactory()
                        .geoToPixel(toPosition(entry.getKey()), mapViewer.getZoom());
                    if (showBusynessHeatmap) {
                        final int radius;
                        if (showCleanlinessHeatmap) {
                            radius = scaledHeatRadius(mapViewer, HEAT_RADIUS_LARGE);
                        }
                        else {
                            radius = scaledHeatRadius(mapViewer, HEAT_RADIUS_MEDIUM);
                        }
                        drawHeat(canvas, point, averageReportedValue(entry.getValue(), true), radius);
                    }
                    if (showCleanlinessHeatmap) {
                        final int radius;
                        if (showBusynessHeatmap) {
                            radius = scaledHeatRadius(mapViewer, HEAT_RADIUS_SMALL);
                        }
                        else {
                            radius = scaledHeatRadius(mapViewer, HEAT_RADIUS_MEDIUM);
                        }
                        drawHeat(canvas, point, averageReportedValue(entry.getValue(), false), radius);
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
                .mapToDouble(value -> reportedValue(value, busyness))
                .filter(value -> {
                    return !Double.isNaN(value);
                })
                .average()
                .orElse(Double.NaN);
        }

        private static double reportedValue(final HeatmapData value, final boolean busyness) {
            final double result;
            if (busyness) {
                result = value.busyness();
            }
            else {
                result = value.cleanliness();
            }
            return result;
        }

        private void drawHeat(final Graphics2D canvas, final Point2D point, final double value, final int radius) {
            final Color color = heatColor(value);
            final float[] stops = {0f, .55f, 1f};
            final Color[] colors = {withAlpha(color, 150), withAlpha(color, 72), withAlpha(color, 0)};
            canvas.setPaint(new RadialGradientPaint((float) point.getX(), (float) point.getY(), radius, stops, colors));
            canvas.fillOval((int) point.getX() - radius, (int) point.getY() - radius, radius * 2, radius * 2);
        }

        private void drawRoute(final Graphics2D canvas, final JXMapViewer mapViewer) {
            if (route.size() >= 2) {
                final Path2D path = new Path2D.Double();
                for (int index = 0; index < route.size(); index++) {
                    final Point2D point = mapViewer
                        .getTileFactory()
                        .geoToPixel(toPosition(route.get(index)), mapViewer.getZoom());
                    if (index == 0) {
                        path.moveTo(point.getX(), point.getY());
                    }
                    else {
                        path.lineTo(point.getX(), point.getY());
                    }
                }
                canvas.setColor(new Color(COLOR_CHANNEL_MAX, COLOR_CHANNEL_MAX, COLOR_CHANNEL_MAX, MARKER_ALPHA));
                canvas.setStroke(new BasicStroke(MARKER_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                canvas.draw(path);
                canvas.setColor(MAP_LOW);
                canvas.setStroke(new BasicStroke(STANDARD_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                canvas.draw(path);
            }
        }

        private void drawPoint(final Graphics2D canvas, final JXMapViewer mapViewer, final Rectangle viewport,
                               final PointData pointData) {
            final Point2D point = mapViewer
                .getTileFactory()
                .geoToPixel(toPosition(pointData.geoPoint()), mapViewer.getZoom());
            final int x = (int) point.getX();
            final int y = (int) point.getY();
            if (pointData.selected()) {
                canvas.setColor(Color.WHITE);
                canvas.fillOval(x - MARKER_RADIUS, y - MARKER_RADIUS, MARKER_DIAMETER, MARKER_DIAMETER);
            }
            canvas.setColor(pointData.color());
            final int radius;
            if (pointData.selected()) {
                radius = SMALL_MARKER_RADIUS;
            }
            else {
                radius = CARD_PADDING;
            }
            canvas.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            if (!pointData.code().isBlank()) {
                canvas.setColor(Color.WHITE);
                canvas.setFont(canvas
                    .getFont()
                    .deriveFont(Font.BOLD, MARKER_CODE_FONT_SIZE));
                canvas.drawString(pointData.code(), x - canvas
                    .getFontMetrics()
                    .stringWidth(pointData.code()) / 2, y + MAP_ZOOM_LEVEL);
            }
            if (pointData.selected() && !pointData.label().isBlank()) {
                canvas.setFont(canvas
                    .getFont()
                    .deriveFont(Font.BOLD, MARKER_FONT_SIZE));
                final int labelWidth = canvas
                    .getFontMetrics()
                    .stringWidth(pointData.label());
                canvas.setColor(new Color(COLOR_CHANNEL_MAX, COLOR_CHANNEL_MAX, COLOR_CHANNEL_MAX, MARKER_LABEL_ALPHA));
                canvas.fillRoundRect(x + BODY_FONT_SIZE, y - MARKER_LABEL_HEIGHT, labelWidth + CARD_PADDING,
                    MAP_PADDING_VERTICAL, MAP_PADDING_SMALL, MAP_PADDING_SMALL);
                canvas.setColor(Theme.INK);
                canvas.drawString(pointData.label(), x + MAP_PADDING_VERTICAL, y - MARKER_LABEL_OFFSET);
            }
            if (!pointData.washroomId().isBlank()) {
                markerHitTargets.put(pointData.washroomId(), new Rectangle(x - viewport.x - MAP_PADDING_MEDIUM,
                    y - viewport.y - MAP_PADDING_LARGE,
                    STANDARD_SIZE, STANDARD_SIZE));
            }
        }

        private void fitCampus() {
            if (viewer != null && !washrooms.isEmpty() && viewer.getWidth() != 0) {
                final Set<GeoPosition> positions = new HashSet<>();
                positions.add(toPosition(origin));
                for (final Washroom washroom : washrooms) {
                    positions.add(new GeoPosition(washroom
                        .building()
                        .latitude(), washroom
                        .building()
                        .longitude()));
                }
                viewer.zoomToBestFit(positions, TIGHT_MAP_FIT_RATIO);
            }
        }

        private void fitRoute() {
            if (viewer != null && !route.isEmpty() && viewer.getWidth() != 0) {
                final Set<GeoPosition> positions = new HashSet<>();
                for (final GeoPoint parameterValue : route) {
                    positions.add(toPosition(parameterValue));
                }
                viewer.zoomToBestFit(positions, WIDE_MAP_FIT_RATIO);
            }
        }

        public GeoPosition convertPointToGeoPosition(final Point2D parameterValue) {
            return viewer.convertPointToGeoPosition(parameterValue);
        }

        @Override
        public void addMouseListener(final MouseListener listener) {
            if (viewer != null) {
                viewer.addMouseListener(listener);
            }
        }

        @Override
        public void removeMouseListener(final MouseListener listener) {
            if (viewer != null) {
                viewer.removeMouseListener(listener);
            }
        }

        private record PointData(GeoPoint geoPoint, String label, Color color, String code, String washroomId,
                                 boolean selected) {
        }
    }
}
// CSON: MagicNumber
