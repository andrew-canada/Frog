package view;

import entity.GeoPoint;
import entity.Washroom;
import interface_adapter.account.IsLoggedInState;
import interface_adapter.account.IsLoggedInViewModel;
import interface_adapter.directions.MapViewModel;
import interface_adapter.filter.FilterController;
import interface_adapter.filter.FilterViewModel;
import interface_adapter.sort_washrooms.SortWashroomController;
import interface_adapter.sort_washrooms.SortWashroomViewModel;
import interface_adapter.view_reviews.WashroomListViewModel;
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

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class MainView extends JPanel {
    private static final Color MAP_LOW = Theme.COLORBLIND_BLUE;
    private static final Color MAP_HIGH = Theme.COLORBLIND_ORANGE;
    /**
     * Okabe-Ito endpoints keep map values distinguishable with colour-vision deficiencies.
     */
    private final CardLayout buttonsLayout = new CardLayout();
    private final JPanel buttonsPanel = new JPanel(buttonsLayout);
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
    },
            onReport = () -> {
            },
            onBusyness = () -> {
            },
            onAccount = () -> {
            },
            onModerator = () -> {
            };

    private Function<String, GeoPoint> addressLookup = address -> {
        throw new IllegalStateException("Address search is unavailable.");
    };
    private double latitude = 43.6629, longitude = -79.3957;

    /**
     * Retained for callers that do not provide filtering controls.
     */
    public MainView(WashroomListViewModel washrooms, MapViewModel route) {
        this(washrooms, route, new FilterViewModel(), new SortWashroomViewModel(), new IsLoggedInViewModel());
    }

    public MainView(WashroomListViewModel washrooms,
                    MapViewModel route,
                    FilterViewModel filter,
                    SortWashroomViewModel sortWashroom,
                    IsLoggedInViewModel isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
        isLoggedIn.getState().addPropertyChangeListener(e -> render(isLoggedIn.getState()));
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        JComponent loggedIn = headerLoggedIn();
        JComponent loggedOut = headerLoggedOut();
        buttonsPanel.add(loggedOut, "loggedOut");
        buttonsPanel.add(loggedIn, "loggedIn");
        add(buttonsPanel, BorderLayout.NORTH);
        JSplitPane content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar(washrooms), mapArea());
        content.setDividerLocation(290);
        content.setDividerSize(8);
        content.setContinuousLayout(true);
        add(content, BorderLayout.CENTER);
        map.setOnWashroomSelected(this::selectWashroom);
        washrooms.addPropertyChangeListener(e -> renderList(washrooms.getState().items()));
        route.addPropertyChangeListener(e -> {
            MapViewModel.State s = route.getState();
            if (s.success()) {

                routeLabel.setText("<html><b>" + s.distance() + "</b> · about " + s.duration() + " walk · live GraphHopper route</html>");
                map.setRoute(s.points());
            } else {
                routeLabel.setText(s.message());
                map.clearRoute();
            }
        });
        filter.addPropertyChangeListener(e ->
        {
            FilterViewModel.State s = filter.getState();
            if (!s.success()) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), s.message());
            } else {
                map.setWashrooms(s.washrooms());
            }
        });
        sortWashroom.addPropertyChangeListener(e -> {
            SortWashroomViewModel.State state = sortWashroom.getState();
            map.setWashrooms(state.washrooms());
        });
    }

    private JComponent headerLoggedIn() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.PAPER);
        p.setBorder(Theme.pad(10, 18, 10, 18));
        JLabel brand = Theme.label("FlushID", 20, Theme.BLUE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD));
        p.add(brand, BorderLayout.WEST);
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nav.setOpaque(false);
        moderatorNav = nav("Moderator", () -> onModerator.run());
        moderatorNav.setVisible(false); // hidden until a moderator logs in
        for (JButton b : new JButton[]{nav("Account", () -> onAccount.run()), nav("Report status", () -> onReport.run()), nav("View status", () -> onBusyness.run()), moderatorNav})
            nav.add(b);
        p.add(nav, BorderLayout.EAST);
        return p;
    }

    private JComponent headerLoggedOut() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.PAPER);
        p.setBorder(Theme.pad(10, 18, 10, 18));
        JLabel brand = Theme.label("FlushID", 20, Theme.BLUE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD));
        p.add(brand, BorderLayout.WEST);
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nav.setOpaque(false);
        for (JButton b : new JButton[]{nav("Report status", () -> onReport.run()), nav("View status", () -> onBusyness.run()), nav("Login", () -> onLogin.run())})
            nav.add(b);
        p.add(nav, BorderLayout.EAST);
        return p;
    }

    private JButton nav(String text, Runnable action) {
        JButton b = Theme.button(text);
        b.addActionListener(e -> action.run());
        return b;
    }

    private JComponent sidebar(WashroomListViewModel washrooms) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setPreferredSize(new Dimension(290, 0));
        p.setBackground(Theme.PAPER);
        p.setBorder(Theme.pad(14, 18, 14, 12));
        JPanel controls = new JPanel(new GridLayout(3, 2, 8, 8));
        controls.setOpaque(false);
        JButton location = Theme.button("Location"), filters = Theme.button("Filters");
        JButton clear = Theme.button("Clear Filters");
        controls.add(location);
        controls.add(filters);
        controls.add(new JLabel());
        controls.add(clear);
        controls.add(new JLabel("Sort by:"));
        WashroomSortDropdownControl washroomSortDropdownControl = new WashroomSortDropdownControl();
        controls.add(washroomSortDropdownControl);
        p.add(controls, BorderLayout.NORTH);
        location.addActionListener(e -> new LocationInputDialog(SwingUtilities.getWindowAncestor(this), addressLookup, (lat, lng) -> {
            latitude = lat;
            longitude = lng;
            map.setOrigin(new GeoPoint(lat, lng));
            routeLabel.setText("Location updated — choose directions");
        }).setVisible(true));
        filters.addActionListener(e ->
                new FilterView(SwingUtilities.getWindowAncestor(this),
                        "Filter",
                        selectedId(),
                        filterController,
                        latitude,
                        longitude).setVisible(true));
        washroomSortDropdownControl.addActionListener(e -> sortWashroomController.execute(
                washroomSortDropdownControl.getSelectedItem().toString(),
                latitude,
                longitude));
        clear.addActionListener(e -> filterController.execute(5, 1, false, false, false, selectedId(), null, latitude, longitude));
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Theme.PAPER);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(32);
        scroll.getVerticalScrollBar().setBlockIncrement(192);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JComponent mapArea() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Theme.PAPER);
        p.setBorder(Theme.pad(14, 12, 14, 18));
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.CREAM);
        bar.setBorder(Theme.pad(10, 12, 10, 12));
        JPanel mapStatus = new JPanel();
        mapStatus.setOpaque(false);
        mapStatus.setLayout(new BoxLayout(mapStatus, BoxLayout.Y_AXIS));
        mapStatus.add(routeLabel);
        heatmapLegend.setVisible(false);
        heatmapLegend.setAlignmentX(Component.LEFT_ALIGNMENT);
        mapStatus.add(heatmapLegend);
        bar.add(mapStatus, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
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
        JButton clear = Theme.button("Clear route");
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
        busynessHeatmap.setText("Busyness heatmap" + (busynessHeatmapVisible ? " (On)" : ""));
        cleanlinessHeatmap.setText("Cleanliness heatmap" + (cleanlinessHeatmapVisible ? " (On)" : ""));
        if (busynessHeatmapVisible && cleanlinessHeatmapVisible) {
            heatmapLegend.setText("Heatmap: busyness outer glow · cleanliness inner glow · blue = lower · orange = higher · gray = no data");
        } else if (busynessHeatmapVisible) {
            heatmapLegend.setText("Busyness heatmap: blue = less busy · orange = more busy · gray = no data");
        } else if (cleanlinessHeatmapVisible) {
            heatmapLegend.setText("Cleanliness heatmap: blue = lower · orange = higher · gray = no data");
        }
        heatmapLegend.setVisible(busynessHeatmapVisible || cleanlinessHeatmapVisible);
        map.setHeatmapVisibility(busynessHeatmapVisible, cleanlinessHeatmapVisible);
    }

    public void renderList(List<WashroomListViewModel.Item> items) {
        renderedItems = List.copyOf(items);
        list.removeAll();
        cardsByWashroomId.clear();
        if (!selectedId.isBlank() && items.stream().noneMatch(item -> item.id().equals(selectedId)))
            selectedId = "";
        map.setSelectedWashroom(selectedId);
        for (var item : items) {
            JPanel card = new JPanel(new BorderLayout(4, 4));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 124));
            card.setBackground(item.id().equals(selectedId) ? Theme.PALE_BLUE : Theme.PAPER);
            card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(item.id().equals(selectedId) ? Theme.BLUE : Theme.LINE), Theme.pad(10, 10, 10, 10)));
            JLabel name = Theme.label(item.name(), 14, item.id().equals(selectedId) ? Theme.BLUE : Theme.INK);
            name.setFont(name.getFont().deriveFont(Font.BOLD));
            card.add(name, BorderLayout.NORTH);
            JLabel description = Theme.label(item.description(), 12, Theme.INK);
            description.setFont(description.getFont().deriveFont(Font.BOLD));
            description.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel details = Theme.label(String.format("★ %.1f · %d m away", item.rating(), item.distanceMeters()), 12, Theme.MUTED);
            JPanel information = new JPanel();
            information.setLayout(new BoxLayout(information, BoxLayout.Y_AXIS));
            information.setOpaque(false);
            information.add(description);
            information.add(Box.createVerticalStrut(4));
            details.setAlignmentX(Component.LEFT_ALIGNMENT);
            information.add(details);
            card.add(information, BorderLayout.CENTER);
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            actions.setOpaque(false);
            JButton reviews = Theme.button("Reviews"), directions = Theme.button("Directions");
            reviews.setPreferredSize(new Dimension(78, 28));
            directions.setPreferredSize(new Dimension(94, 28));
            MouseAdapter select = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
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

    private void selectWashroom(String id) {
        if (id == null || id.isBlank() || id.equals(selectedId)) return;
        selectedId = id;
        renderList(renderedItems);
        scrollSelectedCardIntoView();
    }

    private void scrollSelectedCardIntoView() {
        JPanel selectedCard = cardsByWashroomId.get(selectedId);
        if (selectedCard == null) return;
        SwingUtilities.invokeLater(() -> selectedCard.scrollRectToVisible(
                new Rectangle(0, 0, selectedCard.getWidth(), selectedCard.getHeight())));
    }

    public void setWashrooms(List<Washroom> washrooms) {
        map.setWashrooms(washrooms);
    }

    /**
     * Supplies the latest reported values used by the optional map heatmap layers.
     */
    public void setHeatmapData(List<HeatmapData> values) {
        map.setHeatmapData(values);
    }

    public void setAddressLookup(Function<String, GeoPoint> lookup) {
        addressLookup = lookup == null ? address -> {
            throw new IllegalStateException("Address search is unavailable.");
        } : lookup;
    }

    public void setOnReviews(Consumer<String> c) {
        onReviews = c;
    }

    public void setOnDirections(Consumer<String> c) {
        onDirections = c;
    }

    public void setOnLogin(Runnable r) {
        onLogin = r;
    }

    public void setOnAccount(Runnable r) {
        onAccount = r;
    }

    public void setOnReport(Runnable r) {
        onReport = r;
    }

    public void setOnBusyness(Runnable r) {
        onBusyness = r;
    }

    public void setOnModerator(Runnable r) {
        onModerator = r;
    }

    public void setFilterController(FilterController f) {
        filterController = f;
    }

    public void setSortWashroomController(SortWashroomController s) {sortWashroomController = s;}

    /** Shows the Moderator nav entry only for a user with moderator privileges. */
    public void setModerator(boolean isModerator) {
        if (moderatorNav != null) {
            moderatorNav.setVisible(isModerator);
        }
    }

    /**
     * Reflects the number of reported reviews awaiting moderation on the Moderator nav button:
     * appends the count and accents the label (berry, bold) when there is a queue, plain otherwise.
     */
    public void setModeratorReportCount(int n) {
        if (moderatorNav == null) {
            return;
        }
        moderatorNav.setText(n > 0 ? "Moderator (" + n + ")" : "Moderator");
        moderatorNav.setForeground(n > 0 ? Theme.BERRY : Color.BLACK);
        moderatorNav.setFont(moderatorNav.getFont().deriveFont(n > 0 ? Font.BOLD : Font.PLAIN));
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

    private void render(IsLoggedInState state) {

        if (state.getIsLoggedIn()) {
            buttonsLayout.show(buttonsPanel, "loggedIn");
        } else {
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

    private static final class CampusMapPanel extends JPanel {
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

            TileFactoryInfo tileInfo = new OSMTileFactoryInfo();
            File cacheDirectory = new File(System.getProperty("user.home"), ".flushid/map-cache");
            DefaultTileFactory tileFactory = new DefaultTileFactory(tileInfo);
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

            MouseInputListener pan = new PanMouseInputListener(viewer);
            viewer.addMouseListener(pan);
            viewer.addMouseMotionListener(pan);
            viewer.addMouseListener(new CenterMapListener(viewer));
            viewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(viewer));
            viewer.addKeyListener(new PanKeyListener(viewer));
            viewer.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    if (!SwingUtilities.isLeftMouseButton(event)) return;
                    for (Map.Entry<String, Rectangle> target : markerHitTargets.entrySet()) {
                        if (target.getValue().contains(event.getPoint())) {
                            onWashroomSelected.accept(target.getKey());
                            return;
                        }
                    }
                }
            });
            viewer.setFocusable(true);

            JLabel attribution = Theme.label("Drag to pan · mouse wheel to zoom · © OpenStreetMap contributors · routes by GraphHopper", 10, Theme.MUTED);
            attribution.setBorder(Theme.pad(4, 8, 4, 8));
            add(viewer, BorderLayout.CENTER);
            add(attribution, BorderLayout.SOUTH);
        }

        /**
         * Keeps each glow approximately the same real-world size as the map zoom changes.
         */
        private static int scaledHeatRadius(JXMapViewer map, int baseRadius) {
            double zoomFactor = Math.pow(2, 3 - map.getZoom());
            return (int) Math.round(Math.max(24, Math.min(180, baseRadius * zoomFactor)));
        }

        private static Color heatColor(double value) {
            if (Double.isNaN(value)) return Theme.NO_DATA;
            double progress = Math.max(0, Math.min(1, (value - 1) / 4));
            return new Color(
                    (int) Math.round(MAP_LOW.getRed() + (MAP_HIGH.getRed() - MAP_LOW.getRed()) * progress),
                    (int) Math.round(MAP_LOW.getGreen() + (MAP_HIGH.getGreen() - MAP_LOW.getGreen()) * progress),
                    (int) Math.round(MAP_LOW.getBlue() + (MAP_HIGH.getBlue() - MAP_LOW.getBlue()) * progress));
        }

        private static Color withAlpha(Color color, int alpha) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        }

        private static GeoPosition toPosition(GeoPoint point) {
            return new GeoPosition(point.latitude(), point.longitude());
        }

        void setWashrooms(List<Washroom> values) {
            washrooms = List.copyOf(values);
            if (viewer != null) {
                viewer.repaint();
                SwingUtilities.invokeLater(this::fitCampus);
            }
        }

        void setHeatmapData(List<HeatmapData> values) {
            Map<String, HeatmapData> byWashroomId = new HashMap<>();
            for (HeatmapData value : values) {
                byWashroomId.put(value.washroomId(), value);
            }
            heatmapData = Map.copyOf(byWashroomId);
            if (viewer != null) viewer.repaint();
        }

        void setHeatmapVisibility(boolean busynessVisible, boolean cleanlinessVisible) {
            showBusynessHeatmap = busynessVisible;
            showCleanlinessHeatmap = cleanlinessVisible;
            if (viewer != null) viewer.repaint();
        }

        void setSelectedWashroom(String id) {
            selectedWashroomId = id == null ? "" : id;
            if (viewer != null) viewer.repaint();
        }

        void setOnWashroomSelected(Consumer<String> action) {
            onWashroomSelected = action == null ? id -> {
            } : action;
        }

        void setOrigin(GeoPoint value) {
            origin = value;
            if (viewer != null) viewer.repaint();
        }

        void setRoute(List<GeoPoint> points) {
            route = List.copyOf(points);
            if (viewer != null) {
                viewer.repaint();
                SwingUtilities.invokeLater(this::fitRoute);
            }
        }

        void clearRoute() {
            route = List.of();
            if (viewer != null) viewer.repaint();
        }

        private void paintOverlay(Graphics2D graphics, JXMapViewer map, int width, int height) {
            Graphics2D canvas = (Graphics2D) graphics.create();
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle viewport = map.getViewportBounds();
            canvas.translate(-viewport.x, -viewport.y);
            markerHitTargets.clear();
            Map<GeoPoint, List<Washroom>> washroomsByLocation = washroomsByLocation();
            drawHeatmaps(canvas, map, washroomsByLocation);
            drawRoute(canvas, map);
            drawPoint(canvas, map, viewport, origin, "You", MAP_LOW, "", "", false);
            for (List<Washroom> washroomsAtLocation : washroomsByLocation.values()) {
                Washroom representative = washroomsAtLocation.getFirst();
                boolean selected = washroomsAtLocation.stream()
                        .anyMatch(washroom -> washroom.id().equals(selectedWashroomId));
                drawPoint(canvas, map, viewport,
                        new GeoPoint(representative.building().latitude(), representative.building().longitude()),
                        representative.building().name(), selected ? MAP_LOW : MAP_HIGH,
                        representative.building().code(), representative.id(), selected);
            }
            canvas.dispose();
        }

        private Map<GeoPoint, List<Washroom>> washroomsByLocation() {
            Map<GeoPoint, List<Washroom>> values = new LinkedHashMap<>();
            for (Washroom washroom : washrooms) {
                GeoPoint location = new GeoPoint(washroom.building().latitude(), washroom.building().longitude());
                values.computeIfAbsent(location, ignored -> new ArrayList<>()).add(washroom);
            }
            return values;
        }

        private void drawHeatmaps(Graphics2D canvas, JXMapViewer map,
                                  Map<GeoPoint, List<Washroom>> washroomsByLocation) {
            if (!showBusynessHeatmap && !showCleanlinessHeatmap) return;
            for (Map.Entry<GeoPoint, List<Washroom>> entry : washroomsByLocation.entrySet()) {
                Point2D point = map.getTileFactory().geoToPixel(toPosition(entry.getKey()), map.getZoom());
                if (showBusynessHeatmap) {
                    drawHeat(canvas, point, averageReportedValue(entry.getValue(), true),
                            scaledHeatRadius(map, showCleanlinessHeatmap ? 86 : 70));
                }
                if (showCleanlinessHeatmap) {
                    drawHeat(canvas, point, averageReportedValue(entry.getValue(), false),
                            scaledHeatRadius(map, showBusynessHeatmap ? 54 : 70));
                }
            }
        }

        private double averageReportedValue(List<Washroom> washroomsAtLocation, boolean busyness) {
            return washroomsAtLocation.stream()
                    .map(washroom -> heatmapData.get(washroom.id()))
                    .filter(java.util.Objects::nonNull)
                    .mapToDouble(value -> busyness ? value.busyness() : value.cleanliness())
                    .filter(value -> !Double.isNaN(value))
                    .average()
                    .orElse(Double.NaN);
        }

        private void drawHeat(Graphics2D canvas, Point2D point, double value, int radius) {
            Color color = heatColor(value);
            float[] stops = {0f, .55f, 1f};
            Color[] colors = {withAlpha(color, 150), withAlpha(color, 72), withAlpha(color, 0)};
            canvas.setPaint(new RadialGradientPaint((float) point.getX(), (float) point.getY(), radius, stops, colors));
            canvas.fillOval((int) point.getX() - radius, (int) point.getY() - radius, radius * 2, radius * 2);
        }

        private void drawRoute(Graphics2D canvas, JXMapViewer map) {
            if (route.size() < 2) return;
            Path2D path = new Path2D.Double();
            for (int index = 0; index < route.size(); index++) {
                Point2D point = map.getTileFactory().geoToPixel(toPosition(route.get(index)), map.getZoom());
                if (index == 0) path.moveTo(point.getX(), point.getY());
                else path.lineTo(point.getX(), point.getY());
            }
            canvas.setColor(new Color(255, 255, 255, 210));
            canvas.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            canvas.draw(path);
            canvas.setColor(MAP_LOW);
            canvas.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            canvas.draw(path);
        }

        private void drawPoint(Graphics2D canvas, JXMapViewer map, Rectangle viewport, GeoPoint geoPoint,
                               String label, Color color, String code, String washroomId, boolean selected) {
            Point2D point = map.getTileFactory().geoToPixel(toPosition(geoPoint), map.getZoom());
            int x = (int) point.getX(), y = (int) point.getY();
            if (selected) {
                canvas.setColor(Color.WHITE);
                canvas.fillOval(x - 14, y - 14, 28, 28);
            }
            canvas.setColor(color);
            int radius = selected ? 11 : 10;
            canvas.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            if (!code.isBlank()) {
                canvas.setColor(Color.WHITE);
                canvas.setFont(canvas.getFont().deriveFont(Font.BOLD, 8f));
                canvas.drawString(code, x - canvas.getFontMetrics().stringWidth(code) / 2, y + 3);
            }
            if (selected && !label.isBlank()) {
                canvas.setFont(canvas.getFont().deriveFont(Font.BOLD, 11f));
                int labelWidth = canvas.getFontMetrics().stringWidth(label);
                canvas.setColor(new Color(255, 255, 255, 225));
                canvas.fillRoundRect(x + 13, y - 20, labelWidth + 10, 18, 8, 8);
                canvas.setColor(Theme.INK);
                canvas.drawString(label, x + 18, y - 7);
            }
            if (!washroomId.isBlank()) {
                markerHitTargets.put(washroomId, new Rectangle(x - viewport.x - 16, y - viewport.y - 24,
                        32, 32));
            }
        }

        private void fitCampus() {
            if (viewer == null || washrooms.isEmpty() || viewer.getWidth() == 0) return;
            Set<GeoPosition> positions = new HashSet<>();
            positions.add(toPosition(origin));
            for (Washroom washroom : washrooms)
                positions.add(new GeoPosition(washroom.building().latitude(), washroom.building().longitude()));
            viewer.zoomToBestFit(positions, .72);
        }

        private void fitRoute() {
            if (viewer == null || route.isEmpty() || viewer.getWidth() == 0) return;
            Set<GeoPosition> positions = new HashSet<>();
            for (GeoPoint point : route) positions.add(toPosition(point));
            viewer.zoomToBestFit(positions, .82);
        }
    }
}
