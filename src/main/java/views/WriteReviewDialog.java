package views;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;

import interface_adapter.write_review.WriteReviewController;
import interface_adapter.write_review.WriteReviewViewModel;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;

/**
 * Small modal editor; persistence and validation remain in the write-review use case.
 */
public final class WriteReviewDialog extends JDialog {
    public WriteReviewDialog(final Window owner, final WriteReviewViewModel model, final WriteReviewController controller,
                             final String washroomId, final String washroomName, final String username, final Runnable onSaved) {
        super(owner, "Write a review", ModalityType.APPLICATION_MODAL);
        final JPanel page = Theme.page();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.add(Theme.title("Write a review"));
        page.add(Theme.label(washroomName, 13, Theme.MUTED));
        page.add(Box.createVerticalStrut(14));

        final JSlider rating = slider();
        final JSlider cleanliness = slider();
        final JTextArea comment = new JTextArea(5, 32);
        comment.setLineWrap(true);
        comment.setWrapStyleWord(true);
        final JLabel message = Theme.label("", 12, Theme.BERRY);
        page.add(new JLabel("Overall rating"));
        page.add(rating);
        page.add(Box.createVerticalStrut(8));
        page.add(new JLabel("Cleanliness"));
        page.add(cleanliness);
        page.add(Box.createVerticalStrut(8));
        page.add(new JLabel("Your review"));
        page.add(new JScrollPane(comment));
        page.add(Box.createVerticalStrut(10));

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        final JButton cancel = Theme.button("Cancel");
        final JButton submit = Theme.primary("Post review");
        cancel.addActionListener(event -> dispose());
        submit.addActionListener(event -> controller.execute(washroomId, username, rating.getValue(),
            cleanliness.getValue(), comment.getText()));
        buttons.add(cancel);
        buttons.add(submit);
        page.add(buttons);
        page.add(message);
        final PropertyChangeListener resultListener = event -> {
            final WriteReviewViewModel.State state = model.getState();
            message.setText(state.message());
            if (state.success()) {
                onSaved.run();
                dispose();
            }
        };
        model.addPropertyChangeListener(resultListener);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent event) {
                model.removePropertyChangeListener(resultListener);
            }
        });
        setContentPane(page);
        pack();
        setLocationRelativeTo(owner);
    }

    private static JSlider slider() {
        final JSlider slider = new JSlider(1, 5, 4);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBackground(Theme.PAPER);
        return slider;
    }
}
