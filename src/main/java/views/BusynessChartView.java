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
            busynessChart.data = model
                .getState()
                .buckets();
            cleanlinessChart.data = model
                .getState()
                .buckets();
            busynessChart.repaint();
            cleanlinessChart.repaint();
            note.setText(model
                .getState()
                .note() + "  -  blue = lower, orange = higher");
        });
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
        List<BusynessStatsOutputData.HourBucket> data = List.of();
        private final String title;
        private final ToDoubleFunction<BusynessStatsOutputData.HourBucket> levelFor;

        Chart(final String title, final ToDoubleFunction<BusynessStatsOutputData.HourBucket> levelFor) {
            this.title = title;
            this.levelFor = levelFor;
            setBackground(Theme.PAPER);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE),
                Theme.pad(PANEL_PADDING, PANEL_HORIZONTAL_PADDING, PANEL_VERTICAL_PADDING, PANEL_HORIZONTAL_PADDING)));
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
                return;
            }
            final Graphics2D x = (Graphics2D) g.create();
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
            final int w = Math.max(3, (right - left - gap * (data.size() - 1)) / data.size());
            final int currentHour = LocalTime
                .now()
                .getHour();
            for (int i = 0; i < data.size(); i++) {
                final var secondValue = data.get(i);
                final double level = Math.max(0, Math.min(5, levelFor.applyAsDouble(secondValue)));
                final int h = (int) ((bottom - top) * (level / 5));
                final int px = left + i * (w + gap);
                final int py = bottom - h;
                final boolean isCurrentHour = secondValue.hour() == currentHour;
                if (isCurrentHour) {
                    x.setColor(Theme.PALE_GREEN);
                    x.fillRoundRect(px - MARKER_HALF_SIZE, top - MARKER_OFFSET, w + MARKER_OFFSET, bottom - top
                        + BAR_HEIGHT, CORNER_RADIUS,
                        CORNER_RADIUS);
                    x.setColor(Theme.BRIGHT_GREEN);
                    final int markerX = px + w / 2;
                    x.fillPolygon(new int[] {markerX - MARKER_HALF_SIZE, markerX + MARKER_HALF_SIZE, markerX},
                        new int[] {top - MARKER_OFFSET, top - MARKER_OFFSET, top - 2},
                        MARKER_POINT_COUNT);
                }
                final float parameterValue = (float) Math.max(0, Math.min(1, (level - 1) / 4));
                x.setColor(blend(Theme.COLORBLIND_BLUE, Theme.COLORBLIND_ORANGE, parameterValue));
                x.fillRoundRect(px, py, w, h, CORNER_RADIUS, CORNER_RADIUS);
                if (isCurrentHour) {
                    x.setColor(Theme.BRIGHT_GREEN);
                    x.setStroke(new BasicStroke(2f));
                    x.drawRoundRect(px, py, Math.max(0, w - 1), Math.max(0, h - 1), CORNER_RADIUS, CORNER_RADIUS);
                }
                x.setColor(Theme.MUTED);
                if (i % 2 == 0 || isCurrentHour) {
                    if (isCurrentHour) {
                        x.setFont(x
                            .getFont()
                            .deriveFont(Font.BOLD, CHART_LABEL_FONT_SIZE));
                    }
                    else {
                        x.setFont(x
                            .getFont()
                            .deriveFont(Font.PLAIN, CHART_LABEL_FONT_SIZE));
                    }
                    if (isCurrentHour) {
                        x.setColor(Theme.BRIGHT_GREEN);
                    }
                    else {
                        x.setColor(Theme.MUTED);
                    }
                    x.drawString(String.format("%d", secondValue.hour()), px, bottom + PANEL_PADDING);
                }
                x.setFont(x
                    .getFont()
                    .deriveFont(Font.PLAIN, CHART_LABEL_FONT_SIZE));
                x.setColor(Theme.MUTED);
                if (isCurrentHour) {
                    x.drawString(String.format("%.1f", level), px - (isCurrentHour ? 2 : 0), py - TITLE_OFFSET);
                }
                else {
                    x.drawString(String.format("%.1f", level), px - (isCurrentHour ? 2 : 0), py - AXIS_LABEL_OFFSET);
                }
            }
            x.dispose();
        }
    }
}
