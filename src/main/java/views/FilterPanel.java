package views;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import entity.Washroom;

public final class FilterPanel extends JPanel {
    private static final int MEN_FILTER_VALUE = 3;
    private static final int LABEL_FONT_SIZE = 12;
    private static final int CONTROL_GAP = 8;
    private static final int SECTION_GAP = 10;
    private final JFrame frame;
    private final JSlider busyness = new JSlider(1, 5, 5);
    private final JSlider cleanliness = new JSlider(1, 5, 1);
    private final JCheckBox accessible = new JCheckBox("Accessible only");
    private final JCheckBox ownReviews = new JCheckBox("Only washrooms that you've reviewed");
    private final JComboBox<String> gender = new JComboBox<>(new String[] {"Any gender", "All-gender", "Women", "Men"});
    private final JCheckBox filterSelectedBuilding = new JCheckBox("Filter to currently selected building");
    private final JCheckBox personalPlan = new JCheckBox("Only washrooms in your personal plan");

    public FilterPanel(final JFrame frame, final String selected, final Runnable onFilter) {
        this.frame = frame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.PAPER);
        setBorder(Theme.pad(LABEL_FONT_SIZE, LABEL_FONT_SIZE, LABEL_FONT_SIZE, LABEL_FONT_SIZE));
        addHeading();
        addSliders();
        addCheckboxes();
        addTypeAndButtons(onFilter);
    }

    private void addHeading() {
        final JLabel heading = Theme.label("Filters", 16, Theme.INK);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD));
        add(heading);
        add(Box.createVerticalStrut(SECTION_GAP));
    }

    private void addSliders() {
        add(Theme.label("Maximum busyness  -  1 quiet - 5 packed", LABEL_FONT_SIZE, Theme.MUTED));
        configureSlider(busyness);
        add(busyness);
        add(Theme.label("Minimum current cleanliness  -  1 poor - 5 spotless", LABEL_FONT_SIZE, Theme.MUTED));
        configureSlider(cleanliness);
        add(cleanliness);
    }

    private static void configureSlider(final JSlider slider) {
        slider.setBackground(Theme.PAPER);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
    }

    private void addCheckboxes() {
        final JPanel first = new JPanel();
        first.add(configure(accessible));
        first.add(configure(ownReviews));
        add(first);
        final JPanel second = new JPanel();
        second.add(configure(filterSelectedBuilding));
        second.add(configure(personalPlan));
        add(second);
    }

    private static JCheckBox configure(final JCheckBox checkbox) {
        checkbox.setBackground(Theme.PAPER);
        return checkbox;
    }

    private void addTypeAndButtons(final Runnable onFilter) {
        add(Box.createVerticalStrut(CONTROL_GAP));
        add(Theme.label("Washroom type", LABEL_FONT_SIZE, Theme.MUTED));
        add(gender);
        final JPanel buttonPanel = new JPanel();
        final JButton cancel = Theme.button("Cancel");
        final JButton confirm = Theme.primary("Filter");
        cancel.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirm.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancel.addActionListener(entryValue -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
        confirm.addActionListener(entryValue -> onFilter.run());
        buttonPanel.add(cancel);
        buttonPanel.add(confirm);
        add(buttonPanel);
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public int busyness() {
        return busyness.getValue();
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public int cleanliness() {
        return cleanliness.getValue();
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public boolean accessibleOnly() {
        return accessible.isSelected();
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public boolean ownReviews() {
        return ownReviews.isSelected();
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public boolean selectedBuilding() {
        return filterSelectedBuilding.isSelected();
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public boolean personalPlan() {
        return personalPlan.isSelected();
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public Washroom.Gender gender() {
        return switch (gender.getSelectedIndex()) {
            case 1 -> Washroom.Gender.ALL_GENDER;
            case 2 -> Washroom.Gender.WOMEN;
            case MEN_FILTER_VALUE -> Washroom.Gender.MEN;
            default -> null;
        };
    }

}
