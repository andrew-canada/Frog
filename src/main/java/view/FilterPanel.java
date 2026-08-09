package view;

import entity.Washroom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public final class FilterPanel extends JPanel {
    private final JFrame frame;
    private final JSlider busyness = new JSlider(1, 5, 5);
    private final JSlider cleanliness = new JSlider(1, 5, 1);
    private final JCheckBox accessible = new JCheckBox("Accessible only");
    private final JCheckBox ownReviews = new JCheckBox("Only washrooms that you've reviewed");
    private final JComboBox<String> gender = new JComboBox<>(new String[]{"Any gender", "All-gender", "Women", "Men"});
    private final JCheckBox filterSelectedBuilding = new JCheckBox("Filter to currently selected building");
    private final JCheckBox personalPlan = new JCheckBox("Only washrooms in your personal plan");

    public FilterPanel(JFrame frame, String selected, Runnable onFilter) {
        this.frame = frame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.PAPER);
        setBorder(Theme.pad(12, 12, 12, 12));
        JLabel heading = Theme.label("Filters", 16, Theme.INK);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD));
        add(heading);
        add(Box.createVerticalStrut(10));

        add(Theme.label("Maximum busyness · 1 quiet — 5 packed", 12, Theme.MUTED));
        busyness.setBackground(Theme.PAPER);
        busyness.setMajorTickSpacing(1);
        busyness.setPaintTicks(true);
        add(busyness);
        add(Theme.label("Minimum current cleanliness · 1 poor — 5 spotless", 12, Theme.MUTED));
        cleanliness.setBackground(Theme.PAPER);
        cleanliness.setMajorTickSpacing(1);
        cleanliness.setPaintTicks(true);
        add(cleanliness);

        JPanel checkboxes = new JPanel();
        accessible.setBackground(Theme.PAPER);
        checkboxes.add(accessible);
        ownReviews.setBackground(Theme.PAPER);
        checkboxes.add(ownReviews);
        add(checkboxes);

        JPanel checkboxes2 = new JPanel();
        filterSelectedBuilding.setBackground(Theme.PAPER);
        checkboxes2.add(filterSelectedBuilding);
        personalPlan.setBackground(Theme.PAPER);
        checkboxes2.add(personalPlan);
        add(checkboxes2);

        add(Box.createVerticalStrut(8));
        add(Theme.label("Washroom type", 12, Theme.MUTED));
        add(gender);

        JPanel buttonPanel = new JPanel();
        JButton cancel = Theme.button("Cancel");
        JButton confirm = Theme.primary("Filter");
        for (JButton button : new JButton[]{cancel, confirm}) button.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancel.addActionListener(e -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
        confirm.addActionListener(e -> onFilter.run());
        buttonPanel.add(cancel);
        buttonPanel.add(confirm);
        add(buttonPanel);
    }

    public int busyness() {
        return busyness.getValue();
    }

    public int cleanliness() {
        return cleanliness.getValue();
    }

    public boolean accessibleOnly() {
        return accessible.isSelected();
    }

    public boolean ownReviews() {
        return ownReviews.isSelected();
    }

    public boolean selectedBuilding() {
        return filterSelectedBuilding.isSelected();
    }

    public boolean personalPlan() { return personalPlan.isSelected(); }

    public Washroom.Gender gender() {
        return switch (gender.getSelectedIndex()) {
            case 1 -> Washroom.Gender.ALL_GENDER;
            case 2 -> Washroom.Gender.WOMEN;
            case 3 -> Washroom.Gender.MEN;
            default -> null;
        };
    }

}
