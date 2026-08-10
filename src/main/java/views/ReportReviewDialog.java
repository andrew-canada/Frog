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
    private static final int SECTION_GAP = 12;
    private static final int DETAILS_MAX_HEIGHT = 80;
    private static final int TINY_GAP = 4;
    private static final int LABEL_FONT_SIZE = 13;
    private static final int CONTROL_GAP = 10;
    private static final int SMALL_GAP = 8;
    private static final int LARGE_GAP = 6;

    private static final String[] REASONS =
        {"Spam", "Offensive or profane language", "Harassment", "Off-topic", "False or misleading information",
            "Other"};

    public ReportReviewDialog(final Window owner, final ReportReviewController controller,
                              final ReportReviewViewModel model, final String reviewId, final String reporter) {
        super(owner, "Report Review", ModalityType.APPLICATION_MODAL);

        final ReportControls controls = createControls();
        final JPanel page = controls.page();
        final List<JCheckBox> boxes = controls.boxes();
        final JTextArea details = controls.details();
        final JButton cancel = controls.cancel();
        final JButton submit = controls.submit();

        cancel.addActionListener(entryValue -> {
            dispose();
        });
        submit.addActionListener(entryValue -> {
            final List<String> reasons = new ArrayList<>();
            for (final JCheckBox box : boxes) {
                if (box.isSelected()) {
                    reasons.add(box.getText());
                }
            }
            controller.report(reviewId, reporter, reasons, details.getText());
        });

        final PropertyChangeListener listener = parameterValue -> {
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
            public void windowClosed(final java.awt.event.WindowEvent entryValue) {
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

    private ReportControls createControls() {
        final JPanel page = Theme.page();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.add(Theme.title("Report Review"));
        page.add(Box.createVerticalStrut(LARGE_GAP));
        page.add(left(Theme.label("Why are you reporting this review?", LABEL_FONT_SIZE, Theme.INK)));
        page.add(Box.createVerticalStrut(SMALL_GAP));
        final List<JCheckBox> boxes = createReasonBoxes(page);
        page.add(Box.createVerticalStrut(CONTROL_GAP));
        page.add(left(Theme.label("Additional details:", LABEL_FONT_SIZE, Theme.INK)));
        page.add(Box.createVerticalStrut(TINY_GAP));
        final JTextArea details = new JTextArea(3, 26);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        final JScrollPane detailsScroll = new JScrollPane(details);
        detailsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, DETAILS_MAX_HEIGHT));
        page.add(detailsScroll);
        page.add(Box.createVerticalStrut(SECTION_GAP));
        final Buttons buttons = createButtons(page);
        return new ReportControls(page, boxes, details, buttons.cancel(), buttons.submit());
    }

    private static List<JCheckBox> createReasonBoxes(final JPanel page) {
        final List<JCheckBox> boxes = new ArrayList<>();
        for (final String reason : REASONS) {
            final JCheckBox box = new JCheckBox(reason);
            box.setBackground(Theme.PAPER);
            box.setAlignmentX(Component.LEFT_ALIGNMENT);
            boxes.add(box);
            page.add(box);
        }
        return boxes;
    }

    private static Buttons createButtons(final JPanel page) {
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(Theme.PAPER);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        final JButton cancel = Theme.button("Cancel");
        final JButton submit = Theme.primary("Submit");
        buttons.add(cancel);
        buttons.add(submit);
        page.add(buttons);
        return new Buttons(cancel, submit);
    }

    private record Buttons(JButton cancel, JButton submit) {
    }

    private record ReportControls(JPanel page, List<JCheckBox> boxes, JTextArea details, JButton cancel,
                                  JButton submit) {
    }
}
