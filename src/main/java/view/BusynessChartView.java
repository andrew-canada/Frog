package view;

import interface_adapter.busyness.BusynessViewModel;
import use_case.busyness.BusynessStatsOutputData;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class BusynessChartView extends JPanel {
    private final Chart chart = new Chart();
    private final JLabel note = Theme.label("", 12, Theme.MUTED);
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
        text.add(Theme.title("When is it busiest?"));
        text.add(Theme.label("Bahen Centre · typical weekday", 13, Theme.MUTED));
        header.add(text);
        JButton back = Theme.button("← Back to map");
        back.addActionListener(e -> onBack.run());
        header.add(back, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        add(chart);
        note.setBorder(Theme.pad(8, 24, 18, 24));
        add(note, BorderLayout.SOUTH);
        model.addPropertyChangeListener(e -> {
            chart.data = model.getState().buckets();
            chart.repaint();
            note.setText(model.getState().note() + " · blue = quieter, berry = busier");
        });
    }

    public void setOnBack(Runnable r) {
        onBack = r;
    }

    private static final class Chart extends JPanel {
        List<BusynessStatsOutputData.HourBucket> data = List.of();

        Chart() {
            setBackground(Theme.PAPER);
            setBorder(Theme.pad(30, 36, 30, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) {
                g.setColor(Theme.MUTED);
                g.drawString("Loading busyness estimate…", 40, 50);
                return;
            }
            Graphics2D x = (Graphics2D) g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int gap = 8, left = 50, top = 40, bottom = getHeight() - 55, w = (getWidth() - left - 30 - gap * (data.size() - 1)) / data.size();
            for (int i = 0; i < data.size(); i++) {
                var b = data.get(i);
                double level = Math.max(0, Math.min(5, b.busynessLevel()));
                int h = (int) ((bottom - top) * (level / 5));
                int px = left + i * (w + gap), py = bottom - h;
                float t = (float) Math.max(0, Math.min(1, (level - 1) / 4));
                x.setColor(blend(Theme.BLUE, Theme.BERRY, t));
                x.fillRoundRect(px, py, w, h, 10, 10);
                x.setColor(Theme.MUTED);
                x.drawString(String.format("%d", b.hour()), px, bottom + 18);
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
