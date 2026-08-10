package views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import entity.MaintenanceIssue;
import interface_adapter.status_report.StatusReportViewModel;

public final class StatusReportView extends JPanel {
    private static final int SUCCESS_COLOR_BLUE = 80;
    private static final int SUCCESS_COLOR_GREEN = 125;
    private static final int SUCCESS_COLOR_RED = 37;
    private static final int SECTION_GAP = 20;
    private static final int CONTROL_GAP = 14;
    private static final int CARD_HEIGHT = 430;
    private static final int CARD_WIDTH = 520;
    private final JSlider busyness = slider();
    private final JSlider cleanliness = slider();
    private final JComboBox<MaintenanceIssue> issue = new JComboBox<>(MaintenanceIssue.values());
    private final JLabel message = Theme.label("Takes less than 10 seconds.", 13, Theme.MUTED);
    private final JLabel washroomName = Theme.label("Select a washroom", 13, Theme.MUTED);
    private Runnable onSubmit = () -> {
    };
    private Runnable onCancel = () -> {
    };

    public StatusReportView(final StatusReportViewModel model) {
        setLayout(new GridBagLayout());
        setBackground(Theme.CREAM);
        final JPanel card = Theme.page();
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(Theme.title("How is it right now?"));
        card.add(washroomName);
        card.add(Box.createVerticalStrut(SECTION_GAP));
        card.add(new JLabel("Busyness   -   1 quiet - 5 packed"));
        card.add(busyness);
        card.add(Box.createVerticalStrut(CONTROL_GAP));
        card.add(new JLabel("Cleanliness   -   1 poor - 5 spotless"));
        card.add(cleanliness);
        card.add(Box.createVerticalStrut(CONTROL_GAP));
        card.add(new JLabel("Maintenance issue"));
        card.add(issue);
        card.add(Box.createVerticalStrut(SECTION_GAP));
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        final JButton submit = Theme.primary("Submit status");
        final JButton cancel = Theme.button("Cancel");
        submit.addActionListener(entryValue -> {
            onSubmit.run();
        });
        cancel.addActionListener(entryValue -> {
            onCancel.run();
        });
        buttons.add(submit);
        buttons.add(cancel);
        card.add(buttons);
        card.add(message);
        add(card);
        model.addPropertyChangeListener(entryValue -> {
            final var s = model.getState();
            if (s.success()) {
                message.setText(s.message() + String.format("  -  current %.1f/5", s.currentBusyness()));
            }
            else {
                message.setText(s.message());
            }
            if (s.success()) {
                message.setForeground(new Color(SUCCESS_COLOR_RED, SUCCESS_COLOR_GREEN, SUCCESS_COLOR_BLUE));
            }
            else {
                message.setForeground(Theme.BERRY);
            }
        });
    }

    private static JSlider slider() {
        final JSlider s = new JSlider(1, 5, 3);
        s.setMajorTickSpacing(1);
        s.setPaintTicks(true);
        s.setPaintLabels(true);
        s.setBackground(Theme.PAPER);
        return s;
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
    public MaintenanceIssue issue() {
        return (MaintenanceIssue) issue.getSelectedItem();
    }

    public void setOnSubmit(final Runnable reviewValue) {
        onSubmit = reviewValue;
    }

    public void setOnCancel(final Runnable reviewValue) {
        onCancel = reviewValue;
    }

    /**
     * Performs this operation.
     *
     * @param name parameter value.
     */
    public void setWashroomName(final String name) {
        washroomName.setText(name);
    }
}
