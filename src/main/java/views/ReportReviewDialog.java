package views;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import interface_adapter.report_review.ReportReviewController;
import interface_adapter.report_review.ReportReviewViewModel;

/**
 * Modal dialog for reporting a review: pick one or more reasons, add optional
 * details, submit. Shows the thank-you message from the ViewModel and closes.
 */
public final class ReportReviewDialog extends JDialog {

    private static final String[] REASONS =
        {"Spam", "Offensive or profane language", "Harassment", "Off-topic", "False or misleading information",
            "Other"};

    public ReportReviewDialog(final Window owner, final ReportReviewController controller,
                              final ReportReviewViewModel model, final String reviewId, final String reporter) {
        super(owner, "Report Review", ModalityType.APPLICATION_MODAL);

        final JPanel page = Theme.page();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.add(Theme.title("Report Review"));
        page.add(Box.createVerticalStrut(6));
        page.add(left(Theme.label("Why are you reporting this review?", 13, Theme.INK)));
        page.add(Box.createVerticalStrut(8));

        final List<JCheckBox> boxes = new ArrayList<>();
        for (final String reason : REASONS) {
            final JCheckBox box = new JCheckBox(reason);
            box.setBackground(Theme.PAPER);
            box.setAlignmentX(Component.LEFT_ALIGNMENT);
            boxes.add(box);
            page.add(box);
        }

        page.add(Box.createVerticalStrut(10));
        page.add(left(Theme.label("Additional details:", 13, Theme.INK)));
        page.add(Box.createVerticalStrut(4));
        final JTextArea details = new JTextArea(3, 26);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        final JScrollPane detailsScroll = new JScrollPane(details);
        detailsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        page.add(detailsScroll);

        page.add(Box.createVerticalStrut(12));
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(Theme.PAPER);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        final JButton cancel = Theme.button("Cancel");
        final JButton submit = Theme.primary("Submit");
        buttons.add(cancel);
        buttons.add(submit);
        page.add(buttons);

        cancel.addActionListener(e -> {
            dispose();
        });
        submit.addActionListener(e -> {
            final List<String> reasons = new ArrayList<>();
            for (final JCheckBox box : boxes) {
                if (box.isSelected()) {
                    reasons.add(box.getText());
                }
            }
            controller.report(reviewId, reporter, reasons, details.getText());
        });

        final PropertyChangeListener listener = ev -> {
            final ReportReviewViewModel.State state = model.getState();
            if (state.submitted()) {
                JOptionPane.showMessageDialog(this, state.message());
                if (state.success()) {
                    dispose();
                }
            }
        };
        model.addPropertyChangeListener(listener);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(final java.awt.event.WindowEvent e) {
                model.removePropertyChangeListener(listener);
            }
        });

        setContentPane(page);
        pack();
        setLocationRelativeTo(owner);
    }

    private static JComponent left(final JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        return component;
    }
}
