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
    private final JSlider busyness = slider(), cleanliness = slider();
    private final JComboBox<MaintenanceIssue> issue = new JComboBox<>(MaintenanceIssue.values());
    private final JLabel message = Theme.label("Takes less than 10 seconds.", 13, Theme.MUTED);
    private final JLabel washroomName = Theme.label("Select a washroom", 13, Theme.MUTED);
    private Runnable onSubmit = () -> {
    }, onCancel = () -> {
    };

    public StatusReportView(final StatusReportViewModel model) {
        setLayout(new GridBagLayout());
        setBackground(Theme.CREAM);
        final JPanel card = Theme.page();
        card.setPreferredSize(new Dimension(520, 430));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(Theme.title("How is it right now?"));
        card.add(washroomName);
        card.add(Box.createVerticalStrut(20));
        card.add(new JLabel("Busyness  ·  1 quiet — 5 packed"));
        card.add(busyness);
        card.add(Box.createVerticalStrut(14));
        card.add(new JLabel("Cleanliness  ·  1 poor — 5 spotless"));
        card.add(cleanliness);
        card.add(Box.createVerticalStrut(14));
        card.add(new JLabel("Maintenance issue"));
        card.add(issue);
        card.add(Box.createVerticalStrut(20));
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        final JButton submit = Theme.primary("Submit status");
        final JButton cancel = Theme.button("Cancel");
        submit.addActionListener(e -> {
            onSubmit.run();
        });
        cancel.addActionListener(e -> {
            onCancel.run();
        });
        buttons.add(submit);
        buttons.add(cancel);
        card.add(buttons);
        card.add(message);
        add(card);
        model.addPropertyChangeListener(e -> {
            final var s = model.getState();
            if (s.success()) {
                message.setText(s.message() + String.format(" · current %.1f/5", s.currentBusyness()));
            }
            else {
                message.setText(s.message());
            }
            if (s.success()) {
                message.setForeground(new Color(37, 125, 80));
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

    public int busyness() {
        return busyness.getValue();
    }

    public int cleanliness() {
        return cleanliness.getValue();
    }

    public MaintenanceIssue issue() {
        return (MaintenanceIssue) issue.getSelectedItem();
    }

    public void setOnSubmit(final Runnable r) {
        onSubmit = r;
    }

    public void setOnCancel(final Runnable r) {
        onCancel = r;
    }

    public void setWashroomName(final String name) {
        washroomName.setText(name);
    }
}
