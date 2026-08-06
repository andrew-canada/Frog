package view;

import interface_adapter.busyness.BusynessViewModel;
import use_case.busyness.BusynessStatsOutputData;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.ToDoubleFunction;

public final class BusynessChartView extends JPanel {
    private final Chart busynessChart = new Chart("Busyness", BusynessStatsOutputData.HourBucket::busynessLevel);
    private final Chart cleanlinessChart = new Chart("Cleanliness", BusynessStatsOutputData.HourBucket::cleanlinessLevel);
    private final JLabel note = Theme.label("", 12, Theme.MUTED);
    private final JLabel location = Theme.label("Select a washroom · typical weekday", 13, Theme.MUTED);
    private Runnable onBack = () -> {
    };

    public BusynessChartView(BusynessViewModel model) {
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        JPanel header = Theme.page();
        header.setLayout(new BorderLayout());
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Theme.title("Washroom status by hour"));
        text.add(location);
        header.add(text);
        JButton back = Theme.button("← Back to map");
        back.addActionListener(e -> onBack.run());
        header.add(back, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        JPanel charts = new JPanel(new GridLayout(2, 1));
        charts.setBackground(Theme.PAPER);
        charts.add(busynessChart);
        charts.add(cleanlinessChart);
        add(charts);
        note.setBorder(Theme.pad(8, 24, 18, 24));
        add(note, BorderLayout.SOUTH);
        model.addPropertyChangeListener(e -> {
            busynessChart.data = model.getState().buckets();
            cleanlinessChart.data = model.getState().buckets();
            busynessChart.repaint();
            cleanlinessChart.repaint();
            note.setText(model.getState().note() + " · blue = lower, berry = higher");
        });
    }

    public void setOnBack(Runnable r) {
        onBack = r;
    }

    public void setLocationName(String name) {
        location.setText(name + " · typical weekday");
    }

    private static final class Chart extends JPanel {
        private final String title;
        private final ToDoubleFunction<BusynessStatsOutputData.HourBucket> levelFor;
        List<BusynessStatsOutputData.HourBucket> data = List.of();

        Chart(String title, ToDoubleFunction<BusynessStatsOutputData.HourBucket> levelFor) {
            this.title = title;
            this.levelFor = levelFor;
            setBackground(Theme.PAPER);
            setBorder(Theme.pad(18, 36, 18, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) {
                g.setColor(Theme.MUTED);
                g.drawString("Loading status estimate…", 40, 50);
                return;
            }
            Graphics2D x = (Graphics2D) g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            x.setColor(Theme.INK);
            x.setFont(x.getFont().deriveFont(Font.BOLD, 13f));
            x.drawString(title, 0, 14);
            int gap = 4, left = 50, top = 30, bottom = getHeight() - 32;
            int w = Math.max(3, (getWidth() - left - 30 - gap * (data.size() - 1)) / data.size());
            for (int i = 0; i < data.size(); i++) {
                var b = data.get(i);
                double level = Math.max(0, Math.min(5, levelFor.applyAsDouble(b)));
                int h = (int) ((bottom - top) * (level / 5));
                int px = left + i * (w + gap), py = bottom - h;
                float t = (float) Math.max(0, Math.min(1, (level - 1) / 4));
                x.setColor(blend(Theme.BLUE, Theme.BERRY, t));
                x.fillRoundRect(px, py, w, h, 10, 10);
                x.setColor(Theme.MUTED);
                if (i % 2 == 0) x.drawString(String.format("%d", b.hour()), px, bottom + 16);
                x.drawString(String.format("%.1f", level), px, py - 6);
            }
            x.dispose();
        }

        private static Color blend(Color a, Color b, float t) {
            float ratio = Math.max(0, Math.min(1, t));
            return new Color((int) (a.getRed() * (1 - ratio) + b.getRed() * ratio), (int) (a.getGreen() * (1 - ratio) + b.getGreen() * ratio), (int) (a.getBlue() * (1 - ratio) + b.getBlue() * ratio));
        }
    }
}
