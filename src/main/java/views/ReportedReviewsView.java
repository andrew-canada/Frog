package views;

import interface_adapter.moderate_reviews.ModerateReviewsController;
import interface_adapter.moderate_reviews.ModerateReviewsViewModel;
import use_case.moderate_reviews.ReportedReview;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The moderator's Reported Reviews card: shows every reported review as its own
 * card (most-reported first), each with its aggregated report reasons/counts, an
 * expandable list of written details (only offered when some reports include
 * details), and Dismiss / Remove actions.
 */
public final class ReportedReviewsView extends JPanel {

    private static final int MAX_STARS = 5;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final ModerateReviewsViewModel model;
    private final JPanel body = new JPanel();
    /**
     * Review ids whose additional-details section is currently expanded.
     */
    private final Set<String> expanded = new HashSet<>();
    private String moderatorUsername = "moderator";
    private Runnable onBack = () -> {
    };
    private ModerateReviewsController controller;

    public ReportedReviewsView(ModerateReviewsViewModel model) {
        this.model = model;
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        add(header(), BorderLayout.NORTH);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Theme.PAPER);
        body.setBorder(Theme.pad(4, 24, 20, 24));
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        model.addPropertyChangeListener(e -> {
            expanded.clear();
            render();
        });
        render();
    }

    private static JComponent left(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        return component;
    }

    private static JLabel bold(JLabel label) {
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private static String stars(double rating) {
        int filled = Math.max(0, Math.min(MAX_STARS, (int) Math.round(rating)));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < MAX_STARS; i++) {
            builder.append(i < filled ? '★' : '☆');
        }
        return builder.toString();
    }

    private JComponent header() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.PAPER);
        outer.setBorder(Theme.pad(18, 24, 8, 24));
        outer.add(Theme.title("Reported Reviews"), BorderLayout.WEST);
        JButton back = Theme.button("← Back to map");
        back.addActionListener(e -> onBack.run());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(back);
        outer.add(right, BorderLayout.EAST);
        return outer;
    }

    private void render() {
        body.removeAll();
        List<ReportedReview> reviews = model.getState().reportedReviews();
        if (reviews.isEmpty()) {
            body.add(left(Theme.label("No reported reviews to moderate.", 14, Theme.MUTED)));
            String message = model.getState().message();
            if (message != null) {
                body.add(Box.createVerticalStrut(8));
                body.add(left(Theme.label(message, 12, Theme.MUTED)));
            }
            body.revalidate();
            body.repaint();
            return;
        }

        String count = reviews.size() == 1 ? "1 review awaiting moderation"
                : reviews.size() + " reviews awaiting moderation";
        body.add(left(Theme.label(count, 13, Theme.MUTED)));
        body.add(Box.createVerticalStrut(10));

        for (int i = 0; i < reviews.size(); i++) {
            body.add(card(reviews.get(i)));
            if (i < reviews.size() - 1) {
                body.add(Box.createVerticalStrut(12));
            }
        }

        body.revalidate();
        body.repaint();
    }

    private JComponent card(ReportedReview review) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.PAPER);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE), Theme.pad(14, 16, 14, 16)));

        card.add(row(bold(Theme.label(review.washroomName(), 15, Theme.INK)),
                Theme.label(review.totalReports() + " reports", 13, Theme.MUTED)));
        card.add(Box.createVerticalStrut(8));
        card.add(row(Theme.label("Author: " + review.author(), 13, Theme.INK),
                Theme.label("Posted: " + review.date().format(DATE), 13, Theme.MUTED)));
        card.add(left(Theme.label(String.format("Rating: %.1f %s", review.rating(), stars(review.rating())), 13, Theme.INK)));
        card.add(Box.createVerticalStrut(8));
        card.add(left(Theme.label("Review", 13, Theme.MUTED)));
        card.add(left(Theme.label("“" + review.comment() + "”", 14, Theme.INK)));

        card.add(Box.createVerticalStrut(10));
        card.add(left(Theme.label("Report reasons", 13, Theme.MUTED)));
        for (ReportedReview.ReasonCount reasonCount : review.reasonCounts()) {
            card.add(left(Theme.label("• " + reasonCount.reason() + " — " + reasonCount.count(), 13, Theme.INK)));
        }

        int detailsCount = review.additionalDetailsCount();
        if (detailsCount > 0) {
            boolean open = expanded.contains(review.reviewId());
            card.add(Box.createVerticalStrut(8));
            JButton toggle = Theme.button(open
                    ? "Hide Additional Details ▲"
                    : "View Additional Details (" + detailsCount + ") ▼");
            toggle.addActionListener(e -> {
                if (!expanded.remove(review.reviewId())) {
                    expanded.add(review.reviewId());
                }
                render();
            });
            card.add(left(toggle));
            if (open) {
                card.add(Box.createVerticalStrut(6));
                card.add(left(Theme.label("Additional report details", 13, Theme.MUTED)));
                int index = 1;
                for (ReportedReview.AdditionalDetail detail : review.additionalDetails()) {
                    card.add(left(Theme.label(index + ". " + detail.reason(), 13, Theme.INK)));
                    card.add(left(Theme.label("      “" + detail.text() + "”", 13, Theme.MUTED)));
                    index++;
                }
            }
        }

        card.add(Box.createVerticalStrut(14));
        JButton dismiss = Theme.button("Dismiss Reports");
        JButton remove = Theme.primary("Remove Review");
        dismiss.addActionListener(e -> {
            if (controller != null) {
                controller.dismiss(review.reviewId(), moderatorUsername);
            }
        });
        remove.addActionListener(e -> {
            if (controller != null) {
                controller.remove(review.reviewId(), moderatorUsername);
            }
        });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Theme.PAPER);
        actions.add(dismiss);
        actions.add(remove);
        card.add(left(actions));

        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private JPanel row(JComponent left, JComponent right) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.PAPER);
        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void setModeratorUsername(String moderatorUsername) {
        this.moderatorUsername = moderatorUsername;
    }

    public void setController(ModerateReviewsController controller) {
        this.controller = controller;
    }
}
