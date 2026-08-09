package views;

import entity.MaintenanceIssue;
import interface_adapter.status_report.StatusReportViewModel;

import javax.swing.*;
import java.awt.*;

public final class StatusReportView extends JPanel {
    private final JSlider busyness = slider(), cleanliness = slider();
    private final JComboBox<MaintenanceIssue> issue = new JComboBox<>(MaintenanceIssue.values());
    private final JLabel message = Theme.label("Takes less than 10 seconds.", 13, Theme.MUTED);
    private final JLabel washroomName = Theme.label("Select a washroom", 13, Theme.MUTED);
    private Runnable onSubmit = () -> {
    }, onCancel = () -> {
    };

    public StatusReportView(StatusReportViewModel model) {
        setLayout(new GridBagLayout());
        setBackground(Theme.CREAM);
        JPanel card = Theme.page();
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
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        JButton submit = Theme.primary("Submit status"), cancel = Theme.button("Cancel");
        submit.addActionListener(e -> onSubmit.run());
        cancel.addActionListener(e -> onCancel.run());
        buttons.add(submit);
        buttons.add(cancel);
        card.add(buttons);
        card.add(message);
        add(card);
        model.addPropertyChangeListener(e -> {
            var s = model.getState();
            message.setText(s.success() ? s.message() + String.format(" · current %.1f/5", s.currentBusyness()) : s.message());
            message.setForeground(s.success() ? new Color(37, 125, 80) : Theme.BERRY);
        });
    }

    private static JSlider slider() {
        JSlider s = new JSlider(1, 5, 3);
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

    public void setOnSubmit(Runnable r) {
        onSubmit = r;
    }

    public void setOnCancel(Runnable r) {
        onCancel = r;
    }

    public void setWashroomName(String name) {
        washroomName.setText(name);
    }
}
