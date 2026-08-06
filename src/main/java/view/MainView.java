package view;

import entity.GeoPoint;
import entity.Washroom;
import interface_adapter.directions.MapViewModel;
import interface_adapter.view_reviews.WashroomListViewModel;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class MainView extends JPanel {
    private final JPanel list = new JPanel();
    private final JLabel routeLabel = Theme.label("Select a washroom to explore", 13, Theme.MUTED);
    private final CampusMapPanel map = new CampusMapPanel();
    private String selectedId = "";
    private List<WashroomListViewModel.Item> renderedItems = List.of();

    private Consumer<String> onReviews = id -> {
    };
    private Consumer<String> onDirections = id -> {
    };

    private Runnable onLogin = () -> {
    },
            onRecommend = () -> {
            },
            onReport = () -> {
            },
            onBusyness = () -> {
            },
            onAccount = () -> {
            };

    private Function<String, GeoPoint> addressLookup = address -> {
        throw new IllegalStateException("Address search is unavailable.");
    };
    private double latitude = 43.6629, longitude = -79.3957;

    public MainView(WashroomListViewModel washrooms, MapViewModel route) {
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        add(header(), BorderLayout.NORTH);
        add(sidebar(washrooms), BorderLayout.WEST);
        add(mapArea(), BorderLayout.CENTER);
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
    }

    private JComponent header() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.PAPER);
        p.setBorder(Theme.pad(10, 18, 10, 18));
        JLabel brand = Theme.label("FlushID", 20, Theme.BLUE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD));
        p.add(brand, BorderLayout.WEST);
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nav.setOpaque(false);
        for (JButton b : new JButton[]{nav("Recommend", () -> onRecommend.run()), nav("Account", () -> onAccount.run()), nav("Report status", () -> onReport.run()), nav("Busyness", () -> onBusyness.run()), nav("Login", () -> onLogin.run())})
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
        JPanel controls = new JPanel(new GridLayout(2, 2, 8, 8));
        controls.setOpaque(false);
        JButton location = Theme.button("Location"), filters = Theme.button("Filters");
        controls.add(location);
        controls.add(filters);
        controls.add(new JLabel("Sort by:"));
        SortDropdownControl sortDropdownControl = new SortDropdownControl();
        sortDropdownControl.addActionListener(e -> {
            JComboBox<String> cb = (JComboBox<String>) e.getSource();
            String selected = (String) cb.getSelectedItem();
            System.out.println(selected);
            WashroomListViewModel.State currState = washrooms.getState();
            washrooms.setState(new WashroomListViewModel.State(
                    currState.items(),
                    currState.selectedId(),
                    sortDropdownControl.getSelectedItem().toString(),
                    currState.routeVisible()));
            Comparator<WashroomListViewModel.Item> comparator;
            if (sortDropdownControl.getSelectedItem().toString().equals("Highest rated")) {
                comparator = WashroomListViewModel.Item.BY_RATING;
            } else {
                comparator = WashroomListViewModel.Item.BY_DISTANCE;

            }
            ArrayList<WashroomListViewModel.Item> sortedWashroom = new ArrayList<>(washrooms.getState().items());
            sortedWashroom.sort(comparator);
            renderList(sortedWashroom);
        });
        controls.add(sortDropdownControl);
        p.add(controls, BorderLayout.NORTH);
        location.addActionListener(e -> new LocationInputDialog(SwingUtilities.getWindowAncestor(this), addressLookup, (lat, lng) -> {
            latitude = lat;
            longitude = lng;
            map.setOrigin(new GeoPoint(lat, lng));
            routeLabel.setText("Location updated — choose directions");
        }).setVisible(true));

        filters.addActionListener(e -> JOptionPane.showMessageDialog(this, new FilterPanel(), "Filters", JOptionPane.PLAIN_MESSAGE));
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Theme.PAPER);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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
        bar.add(routeLabel);
        JButton clear = Theme.button("Clear route");
        clear.addActionListener(e -> {
            map.clearRoute();
            routeLabel.setText("Select a washroom to explore");
        });
        bar.add(clear, BorderLayout.EAST);
        p.add(bar, BorderLayout.NORTH);
        p.add(map);
        return p;
    }

    public void renderList(List<WashroomListViewModel.Item> items) {
        renderedItems = List.copyOf(items);
        list.removeAll();
        if (!items.isEmpty() && (selectedId.isBlank() || items.stream().noneMatch(item -> item.id().equals(selectedId))))
            selectedId = items.getFirst().id();
        map.setSelectedWashroom(selectedId);
        for (var item : items) {
            JPanel card = new JPanel(new BorderLayout(4, 4));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 106));
            card.setBackground(item.id().equals(selectedId) ? Theme.PALE_BLUE : Theme.PAPER);
            card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(item.id().equals(selectedId) ? Theme.BLUE : Theme.LINE), Theme.pad(10, 10, 10, 10)));
            JLabel name = Theme.label(item.name(), 14, item.id().equals(selectedId) ? Theme.BLUE : Theme.INK);
            name.setFont(name.getFont().deriveFont(Font.BOLD));
            card.add(name, BorderLayout.NORTH);
            JLabel details = Theme.label(String.format("★ %.1f · %d m away", item.rating(), item.distanceMeters()), 12, Theme.MUTED);
            card.add(details, BorderLayout.CENTER);
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
            details.addMouseListener(select);
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            name.setCursor(card.getCursor());
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
    }

    public void setWashrooms(List<Washroom> washrooms) {
        map.setWashrooms(washrooms);
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

    public void setOnRecommend(Runnable r) {
        onRecommend = r;
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

    private static final class CampusMapPanel extends JPanel {
        private final JXMapViewer viewer;
        private List<GeoPoint> route = List.of();
        private List<Washroom> washrooms = List.of();
        private String selectedWashroomId = "";
        private Consumer<String> onWashroomSelected = id -> {
        };
        private final Map<String, Rectangle> markerHitTargets = new HashMap<>();
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

        void setWashrooms(List<Washroom> values) {
            washrooms = List.copyOf(values);
            if (viewer != null) {
                viewer.repaint();
                SwingUtilities.invokeLater(this::fitCampus);
            }
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
            drawRoute(canvas, map);
            drawPoint(canvas, map, viewport, origin, "You", Theme.BLUE, "", "", false);
            for (Washroom washroom : washrooms) {
                boolean selected = washroom.id().equals(selectedWashroomId);
                drawPoint(canvas, map, viewport, new GeoPoint(washroom.building().latitude(), washroom.building().longitude()),
                        washroom.building().name(), selected ? Theme.BLUE : Theme.BERRY, washroom.building().code(), washroom.id(), selected);
            }
            canvas.dispose();
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
            canvas.setColor(new Color(42, 119, 205));
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
            canvas.setFont(canvas.getFont().deriveFont(Font.BOLD, 11f));
            int labelWidth = canvas.getFontMetrics().stringWidth(label);
            canvas.setColor(new Color(255, 255, 255, 225));
            canvas.fillRoundRect(x + 13, y - 20, labelWidth + 10, 18, 8, 8);
            canvas.setColor(Theme.INK);
            canvas.drawString(label, x + 18, y - 7);
            if (!washroomId.isBlank()) {
                markerHitTargets.put(washroomId, new Rectangle(x - viewport.x - 16, y - viewport.y - 24,
                        labelWidth + 45, 42));
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

        private static GeoPosition toPosition(GeoPoint point) {
            return new GeoPosition(point.latitude(), point.longitude());
        }
    }
}
