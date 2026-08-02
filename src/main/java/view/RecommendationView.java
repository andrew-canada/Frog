package view;

import interface_adapter.recommend.RecommendationViewModel;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public final class RecommendationView extends JPanel {
    private final JCheckBox hurry = new JCheckBox("I’m in a hurry");
    private final JLabel name = Theme.title("Find your best option"), meta = Theme.label("", 14, Theme.MUTED);
    private final JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private Runnable onFind = () -> {
    }, onDirections = () -> {
    }, onReviews = () -> {
    }, onBack = () -> {
    };
    private String selectedId = "";

    public RecommendationView(RecommendationViewModel model) {
        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);
        JPanel top = Theme.page();
        top.setLayout(new BorderLayout());
        top.add(Theme.title("Washroom recommendation"));
        JButton back = Theme.button("← Back to map");
        back.addActionListener(e -> onBack.run());
        top.add(back, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        JPanel card = Theme.page();
        card.setPreferredSize(new Dimension(620, 320));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        hurry.setBackground(Theme.PAPER);
        card.add(hurry);
        card.add(Box.createVerticalStrut(14));
        JButton find = Theme.primary("Find my best washroom");
        find.addActionListener(e -> onFind.run());
        card.add(find);
        card.add(Box.createVerticalStrut(26));
        card.add(Theme.label("YOUR BEST BET", 11, Theme.BLUE));
        card.add(name);
        card.add(meta);
        chips.setBackground(Theme.PAPER);
        card.add(chips);
        card.add(Box.createVerticalStrut(14));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        JButton directions = Theme.primary("Get directions"), reviews = Theme.button("See reviews");
        directions.addActionListener(e -> onDirections.run());
        reviews.addActionListener(e -> onReviews.run());
        actions.add(directions);
        actions.add(reviews);
        card.add(actions);
        center.add(card);
        add(center);
        model.addPropertyChangeListener(e -> render(model.getState()));
    }

    private void render(RecommendationViewModel.State s) {
        if (s.error() != null) {
            name.setText(s.error());
            return;
        }
        if (s.name().isBlank()) return;
        selectedId = s.washroomId();
        name.setText(s.name());
        String live = s.busyness() > 0 ? String.format("busyness %.1f/5", s.busyness()) : "no recent status reports";
        meta.setText(String.format("★ %.1f  ·  %d m  ·  %s  ·  %s%s", s.rating(), s.distanceMeters(), live, s.gender(), s.accessible() ? " · accessible" : ""));
        chips.removeAll();
        for (Map.Entry<String, Double> reason : s.reasons().entrySet()) {
            JLabel chip = Theme.label(reason.getKey() + " +" + String.format("%.2f", reason.getValue()), 12, Theme.BLUE);
            chip.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BLUE), Theme.pad(5, 8, 5, 8)));
            chips.add(chip);
        }
        chips.revalidate();
    }

    public boolean inAHurry() {
        return hurry.isSelected();
    }

    public String selectedId() {
        return selectedId;
    }

    public void setOnFind(Runnable r) {
        onFind = r;
    }

    public void setOnDirections(Runnable r) {
        onDirections = r;
    }

    public void setOnReviews(Runnable r) {
        onReviews = r;
    }

    public void setOnBack(Runnable r) {
        onBack = r;
    }
}
