package views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.LocalTime;
import java.util.List;
import java.util.function.ToDoubleFunction;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import interface_adapter.busyness.BusynessViewModel;
import use_case.busyness.BusynessStatsOutputData;

public final class BusynessChartView extends JPanel {
    private static final int AXIS_LABEL_OFFSET = 6;
    private static final int TITLE_OFFSET = 14;
    private static final float CHART_LABEL_FONT_SIZE = 11f;
    private static final int PANEL_PADDING = 16;
    private static final int CORNER_RADIUS = 10;
    private static final int MARKER_POINT_COUNT = 3;
    private static final int MARKER_OFFSET = 8;
    private static final int MARKER_HALF_SIZE = 4;
    private static final int BAR_HEIGHT = 34;
    private static final float CHART_TITLE_FONT_SIZE = 13f;
    private static final int LOADING_TEXT_X = 40;
    private static final int LOADING_TEXT_Y = 50;
    private static final int PANEL_HORIZONTAL_PADDING = 24;
    private static final int PANEL_VERTICAL_PADDING = 18;
    private final Chart busynessChart = new Chart("Busyness", BusynessStatsOutputData.HourBucket::busynessLevel);
    private final Chart cleanlinessChart =
        new Chart("Cleanliness", BusynessStatsOutputData.HourBucket::cleanlinessLevel);
    private final JLabel note = Theme.label("", 12, Theme.MUTED);
    private final JLabel location = Theme.label("Select firstValue washroom  -  typical weekday", 13, Theme.MUTED);
    private final JLabel currentHourHint =
        Theme.label("Green highlighted column = current hour", 12, Theme.BRIGHT_GREEN);
    private Runnable onBack = () -> {
    };

    public BusynessChartView(final BusynessViewModel model) {
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        final JPanel header = Theme.page();
        header.setLayout(new BorderLayout());
        final JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Theme.title("Washroom status by hour"));
        text.add(location);
        text.add(currentHourHint);
        header.add(text);
        final JButton back = Theme.button("<- Back to map");
        back.addActionListener(entryValue -> {
            onBack.run();
        });
        header.add(back, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        final JPanel charts = new JPanel(new GridLayout(2, 1, 0, 16));
        charts.setBackground(Theme.PAPER);
        charts.setBorder(Theme.pad(MARKER_HALF_SIZE, PANEL_HORIZONTAL_PADDING, PANEL_VERTICAL_PADDING,
            PANEL_HORIZONTAL_PADDING));
        charts.add(busynessChart);
        charts.add(cleanlinessChart);
        add(charts);
        note.setBorder(Theme.pad(MARKER_OFFSET, PANEL_HORIZONTAL_PADDING, PANEL_VERTICAL_PADDING,
            PANEL_HORIZONTAL_PADDING));
        add(note, BorderLayout.SOUTH);
        model.addPropertyChangeListener(entryValue -> {
            updateCharts(model);
        });
    }

    private void updateCharts(final BusynessViewModel model) {
        final List<BusynessStatsOutputData.HourBucket> buckets = model.getState().buckets();
        busynessChart.setData(buckets);
        cleanlinessChart.setData(buckets);
        busynessChart.repaint();
        cleanlinessChart.repaint();
        note.setText(model.getState().note() + "  -  blue = lower, orange = higher");
    }

    public void setOnBack(final Runnable reviewValue) {
        onBack = reviewValue;
    }

    /**
     * Performs this operation.
     *
     * @param name parameter value.
     */
    public void setLocationName(final String name) {
        location.setText(name + "  -  typical weekday");
    }

    private static final class Chart extends JPanel {
        private List<BusynessStatsOutputData.HourBucket> data = List.of();
        private final String title;
        private final ToDoubleFunction<BusynessStatsOutputData.HourBucket> levelFor;

        Chart(final String title, final ToDoubleFunction<BusynessStatsOutputData.HourBucket> levelFor) {
            this.title = title;
            this.levelFor = levelFor;
            setBackground(Theme.PAPER);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE),
                Theme.pad(PANEL_PADDING, PANEL_HORIZONTAL_PADDING, PANEL_VERTICAL_PADDING, PANEL_HORIZONTAL_PADDING)));
        }

        void setData(final List<BusynessStatsOutputData.HourBucket> data) {
            this.data = List.copyOf(data);
        }

        private static Color blend(final Color firstValue, final Color secondValue, final float parameterValue) {
            final float ratio = Math.max(0, Math.min(1, parameterValue));
            return new Color((int) (firstValue.getRed() * (1 - ratio) + secondValue.getRed() * ratio),
                (int) (firstValue.getGreen() * (1 - ratio) + secondValue.getGreen() * ratio),
                (int) (firstValue.getBlue() * (1 - ratio) + secondValue.getBlue() * ratio));
        }

        @Override
        protected void paintComponent(final Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) {
                g.setColor(Theme.MUTED);
                g.drawString("Loading status estimate...", LOADING_TEXT_X, LOADING_TEXT_Y);
            }
            else {
                paintData(g);
            }
        }

        private void paintData(final Graphics graphics) {
            final Graphics2D x = (Graphics2D) graphics.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final Insets insets = getInsets();
            x.setColor(Theme.INK);
            x.setFont(x
                .getFont()
                .deriveFont(Font.BOLD, CHART_TITLE_FONT_SIZE));
            x.drawString(title, insets.left, insets.top + TITLE_OFFSET);
            final int gap = 4;
            final int left = insets.left + 40;
            final int top = insets.top + 36;
            final int bottom = getHeight() - insets.bottom - 30;
            final int right = getWidth() - insets.right - 12;
            final int width = Math.max(3, (right - left - gap * (data.size() - 1)) / data.size());
            final int currentHour = LocalTime.now().getHour();
            final Geometry geometry = new Geometry(left, top, bottom, width, gap, currentHour);
            for (int i = 0; i < data.size(); i++) {
                paintBar(x, data.get(i), i, geometry);
            }
            x.dispose();
        }

        private void paintBar(final Graphics2D graphics, final BusynessStatsOutputData.HourBucket bucket,
                              final int index, final Geometry geometry) {
            final double level = Math.max(0, Math.min(5, levelFor.applyAsDouble(bucket)));
            final int height = (int) ((geometry.bottom() - geometry.top()) * (level / 5));
            final int x = geometry.left() + index * (geometry.width() + geometry.gap());
            final int y = geometry.bottom() - height;
            final boolean current = bucket.hour() == geometry.currentHour();
            final BarPosition position = new BarPosition(x, y, height, level, current, index, geometry);
            if (current) {
                paintCurrentMarker(graphics, position);
            }
            final float ratio = (float) Math.max(0, Math.min(1, (level - 1) / 4));
            graphics.setColor(blend(Theme.COLORBLIND_BLUE, Theme.COLORBLIND_ORANGE, ratio));
            graphics.fillRoundRect(position.x(), position.y(), geometry.width(), position.height(), CORNER_RADIUS,
                CORNER_RADIUS);
            if (current) {
                graphics.setColor(Theme.BRIGHT_GREEN);
                graphics.setStroke(new BasicStroke(2f));
                graphics.drawRoundRect(position.x(), position.y(), Math.max(0, geometry.width() - 1),
                    Math.max(0, position.height() - 1),
                    CORNER_RADIUS, CORNER_RADIUS);
            }
            paintLabels(graphics, bucket, position);
        }

        private static void paintCurrentMarker(final Graphics2D graphics, final BarPosition position) {
            final Geometry geometry = position.geometry();
            graphics.setColor(Theme.PALE_GREEN);
            graphics.fillRoundRect(position.x() - MARKER_HALF_SIZE, geometry.top() - MARKER_OFFSET,
                geometry.width() + MARKER_OFFSET, geometry.bottom() - geometry.top() + BAR_HEIGHT, CORNER_RADIUS,
                CORNER_RADIUS);
            graphics.setColor(Theme.BRIGHT_GREEN);
            final int markerX = position.x() + geometry.width() / 2;
            graphics.fillPolygon(new int[] {markerX - MARKER_HALF_SIZE, markerX + MARKER_HALF_SIZE, markerX},
                new int[] {geometry.top() - MARKER_OFFSET, geometry.top() - MARKER_OFFSET, geometry.top() - 2},
                MARKER_POINT_COUNT);
        }

        private static void paintLabels(final Graphics2D graphics, final BusynessStatsOutputData.HourBucket bucket,
                                        final BarPosition position) {
            final Geometry geometry = position.geometry();
            if (position.index() % 2 == 0 || position.current()) {
                final int fontStyle;
                final Color fontColor;
                if (position.current()) {
                    fontStyle = Font.BOLD;
                    fontColor = Theme.BRIGHT_GREEN;
                }
                else {
                    fontStyle = Font.PLAIN;
                    fontColor = Theme.MUTED;
                }
                graphics.setFont(graphics.getFont().deriveFont(fontStyle, CHART_LABEL_FONT_SIZE));
                graphics.setColor(fontColor);
                graphics.drawString(String.format("%d", bucket.hour()), position.x(),
                    geometry.bottom() + PANEL_PADDING);
            }
            graphics.setFont(graphics.getFont().deriveFont(Font.PLAIN, CHART_LABEL_FONT_SIZE));
            graphics.setColor(Theme.MUTED);
            final int offset;
            if (position.current()) {
                offset = 2;
            }
            else {
                offset = 0;
            }
            final int labelHeight;
            if (position.current()) {
                labelHeight = TITLE_OFFSET;
            }
            else {
                labelHeight = AXIS_LABEL_OFFSET;
            }
            graphics.drawString(String.format("%.1f", position.level()), position.x() - offset,
                position.y() - labelHeight);
        }

        private record Geometry(int left, int top, int bottom, int width, int gap, int currentHour) {
        }

        private record BarPosition(int x, int y, int height, double level, boolean current, int index,
                                   Geometry geometry) {
        }
    }
}
