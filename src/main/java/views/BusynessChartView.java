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
    private final Chart busynessChart = new Chart("Busyness", BusynessStatsOutputData.HourBucket::busynessLevel);
    private final Chart cleanlinessChart =
        new Chart("Cleanliness", BusynessStatsOutputData.HourBucket::cleanlinessLevel);
    private final JLabel note = Theme.label("", 12, Theme.MUTED);
    private final JLabel location = Theme.label("Select a washroom · typical weekday", 13, Theme.MUTED);
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
        final JButton back = Theme.button("← Back to map");
        back.addActionListener(e -> {
            onBack.run();
        });
        header.add(back, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        final JPanel charts = new JPanel(new GridLayout(2, 1, 0, 16));
        charts.setBackground(Theme.PAPER);
        charts.setBorder(Theme.pad(4, 24, 18, 24));
        charts.add(busynessChart);
        charts.add(cleanlinessChart);
        add(charts);
        note.setBorder(Theme.pad(8, 24, 18, 24));
        add(note, BorderLayout.SOUTH);
        model.addPropertyChangeListener(e -> {
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
                .note() + " · blue = lower, orange = higher");
        });
    }

    public void setOnBack(final Runnable r) {
        onBack = r;
    }

    public void setLocationName(final String name) {
        location.setText(name + " · typical weekday");
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
                Theme.pad(16, 24, 18, 24)));
        }

        private static Color blend(final Color a, final Color b, final float t) {
            final float ratio = Math.max(0, Math.min(1, t));
            return new Color((int) (a.getRed() * (1 - ratio) + b.getRed() * ratio),
                (int) (a.getGreen() * (1 - ratio) + b.getGreen() * ratio),
                (int) (a.getBlue() * (1 - ratio) + b.getBlue() * ratio));
        }

        @Override
        protected void paintComponent(final Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) {
                g.setColor(Theme.MUTED);
                g.drawString("Loading status estimate…", 40, 50);
                return;
            }
            final Graphics2D x = (Graphics2D) g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final Insets insets = getInsets();
            x.setColor(Theme.INK);
            x.setFont(x
                .getFont()
                .deriveFont(Font.BOLD, 13f));
            x.drawString(title, insets.left, insets.top + 14);
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
                final var b = data.get(i);
                final double level = Math.max(0, Math.min(5, levelFor.applyAsDouble(b)));
                final int h = (int) ((bottom - top) * (level / 5));
                final int px = left + i * (w + gap);
                final int py = bottom - h;
                final boolean isCurrentHour = b.hour() == currentHour;
                if (isCurrentHour) {
                    x.setColor(Theme.PALE_GREEN);
                    x.fillRoundRect(px - 4, top - 8, w + 8, bottom - top + 34, 10, 10);
                    x.setColor(Theme.BRIGHT_GREEN);
                    final int markerX = px + w / 2;
                    x.fillPolygon(new int[] {markerX - 4, markerX + 4, markerX}, new int[] {top - 8, top - 8, top - 2},
                        3);
                }
                final float t = (float) Math.max(0, Math.min(1, (level - 1) / 4));
                x.setColor(blend(Theme.COLORBLIND_BLUE, Theme.COLORBLIND_ORANGE, t));
                x.fillRoundRect(px, py, w, h, 10, 10);
                if (isCurrentHour) {
                    x.setColor(Theme.BRIGHT_GREEN);
                    x.setStroke(new BasicStroke(2f));
                    x.drawRoundRect(px, py, Math.max(0, w - 1), Math.max(0, h - 1), 10, 10);
                }
                x.setColor(Theme.MUTED);
                if (i % 2 == 0 || isCurrentHour) {
                    x.setFont(x
                        .getFont()
                        .deriveFont(isCurrentHour ? Font.BOLD : Font.PLAIN, 11f));
                    x.setColor(isCurrentHour ? Theme.BRIGHT_GREEN : Theme.MUTED);
                    x.drawString(String.format("%d", b.hour()), px, bottom + 16);
                }
                x.setFont(x
                    .getFont()
                    .deriveFont(Font.PLAIN, 11f));
                x.setColor(Theme.MUTED);
                x.drawString(String.format("%.1f", level), px - (isCurrentHour ? 2 : 0), py - (isCurrentHour ? 14 : 6));
            }
            x.dispose();
        }
    }
}
