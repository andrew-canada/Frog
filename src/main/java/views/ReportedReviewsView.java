package views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import interface_adapter.moderate_reviews.ModerateReviewsController;
import interface_adapter.moderate_reviews.ModerateReviewsViewModel;
import use_case.moderate_reviews.ReportedReview;

/**
 * The moderator's Reported Reviews card: shows every reported review as its own
 * card (most-reported first), each with its aggregated report reasons/counts, an
 * expandable list of written details (only offered when some reports include
 * details), and Dismiss / Remove actions.
 */
public final class ReportedReviewsView extends JPanel {
    private static final int BODY_FONT_SIZE = 14;
    private static final int LABEL_FONT_SIZE = 13;
    private static final int DETAIL_GAP = 6;
    private static final int SMALL_GAP = 8;
    private static final int SECTION_GAP = 10;
    private static final int WASHROOM_NAME_FONT_SIZE = 15;
    private static final int CARD_PADDING = 16;
    private static final int COUNT_FONT_SIZE = 12;
    private static final int CARD_HORIZONTAL_PADDING = 24;
    private static final int CARD_VERTICAL_PADDING = 18;
    private static final int CARD_TOP_PADDING = 20;
    private static final int TINY_GAP = 4;

    private static final int MAX_STARS = 5;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final String QUOTE = "\"";

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

    public ReportedReviewsView(final ModerateReviewsViewModel model) {
        this.model = model;
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        add(header(), BorderLayout.NORTH);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Theme.PAPER);
        body.setBorder(Theme.pad(TINY_GAP, CARD_HORIZONTAL_PADDING, CARD_TOP_PADDING, CARD_HORIZONTAL_PADDING));
        final JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll
            .getVerticalScrollBar()
            .setUnitIncrement(CARD_PADDING);
        add(scroll, BorderLayout.CENTER);
        model.addPropertyChangeListener(entryValue -> {
            expanded.clear();
            render();
        });
        render();
    }

    private static JComponent left(final JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        return component;
    }

    private static JLabel bold(final JLabel label) {
        label.setFont(label
            .getFont()
            .deriveFont(Font.BOLD));
        return label;
    }

    private static String stars(final double rating) {
        final int filled = Math.max(0, Math.min(MAX_STARS, (int) Math.round(rating)));
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < MAX_STARS; i++) {
            if (i < filled) {
                builder.append('*');
            }
            else {
                builder.append('o');
            }
        }
        return builder.toString();
    }

    private JComponent header() {
        final JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.PAPER);
        outer.setBorder(Theme.pad(CARD_VERTICAL_PADDING, CARD_HORIZONTAL_PADDING, SMALL_GAP, CARD_HORIZONTAL_PADDING));
        outer.add(Theme.title("Reported Reviews"), BorderLayout.WEST);
        final JButton back = Theme.button("<- Back to map");
        back.addActionListener(entryValue -> {
            onBack.run();
        });
        final JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(back);
        outer.add(right, BorderLayout.EAST);
        return outer;
    }

    private void render() {
        body.removeAll();
        final List<ReportedReview> reviews = model
            .getState()
            .reportedReviews();
        if (reviews.isEmpty()) {
            renderEmpty();
        }
        else {
            renderReviews(reviews);
        }
        body.revalidate();
        body.repaint();
    }

    private void renderEmpty() {
        body.add(left(Theme.label("No reported reviews to moderate.", BODY_FONT_SIZE, Theme.MUTED)));
        final String message = model.getState().message();
        if (message != null) {
            body.add(Box.createVerticalStrut(SMALL_GAP));
            body.add(left(Theme.label(message, COUNT_FONT_SIZE, Theme.MUTED)));
        }
    }

    private void renderReviews(final List<ReportedReview> reviews) {
        final String count;
        if (reviews.size() == 1) {
            count = "1 review awaiting moderation";
        }
        else {
            count = reviews.size() + " reviews awaiting moderation";
        }
        body.add(left(Theme.label(count, LABEL_FONT_SIZE, Theme.MUTED)));
        body.add(Box.createVerticalStrut(SECTION_GAP));
        for (int i = 0; i < reviews.size(); i++) {
            body.add(card(reviews.get(i)));
            if (i < reviews.size() - 1) {
                body.add(Box.createVerticalStrut(COUNT_FONT_SIZE));
            }
        }
    }

    private JComponent card(final ReportedReview review) {
        final JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.PAPER);
        card.setBorder(
            BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE), Theme.pad(BODY_FONT_SIZE,
                CARD_PADDING, BODY_FONT_SIZE, CARD_PADDING)));
        addReviewContent(card, review);
        addReportReasons(card, review);
        addAdditionalDetails(card, review);
        card.add(createActions(review));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private void addReviewContent(final JPanel card, final ReportedReview review) {
        card.add(row(bold(Theme.label(review.washroomName(), WASHROOM_NAME_FONT_SIZE, Theme.INK)),
            Theme.label(review.totalReports() + " reports", LABEL_FONT_SIZE, Theme.MUTED)));
        card.add(Box.createVerticalStrut(SMALL_GAP));
        card.add(row(Theme.label("Author: " + review.author(), LABEL_FONT_SIZE, Theme.INK),
            Theme.label("Posted: " + review.date().format(DATE), LABEL_FONT_SIZE, Theme.MUTED)));
        card.add(left(Theme.label(String.format("Rating: %.1f %s", review.rating(), stars(review.rating())),
            LABEL_FONT_SIZE, Theme.INK)));
        card.add(Box.createVerticalStrut(SMALL_GAP));
        card.add(left(Theme.label("Review", LABEL_FONT_SIZE, Theme.MUTED)));
        card.add(left(Theme.label(QUOTE + review.comment() + QUOTE, BODY_FONT_SIZE, Theme.INK)));
    }

    private void addReportReasons(final JPanel card, final ReportedReview review) {
        card.add(Box.createVerticalStrut(SECTION_GAP));
        card.add(left(Theme.label("Report reasons", LABEL_FONT_SIZE, Theme.MUTED)));
        for (final ReportedReview.ReasonCount reasonCount : review.reasonCounts()) {
            card.add(left(Theme.label("* " + reasonCount.reason() + " - " + reasonCount.count(), LABEL_FONT_SIZE,
                Theme.INK)));
        }
    }

    private void addAdditionalDetails(final JPanel card, final ReportedReview review) {
        final int detailsCount = review.additionalDetailsCount();
        if (detailsCount > 0) {
            final boolean open = expanded.contains(review.reviewId());
            card.add(Box.createVerticalStrut(SMALL_GAP));
            card.add(left(createDetailsToggle(review, detailsCount, open)));
            if (open) {
                addOpenDetails(card, review);
            }
        }
    }

    private JButton createDetailsToggle(final ReportedReview review, final int detailsCount, final boolean open) {
        final JButton toggle;
        if (open) {
            toggle = Theme.button("Hide Additional Details up");
        }
        else {
            toggle = Theme.button("View Additional Details (" + detailsCount + ") down");
        }
        toggle.addActionListener(entryValue -> {
            if (!expanded.remove(review.reviewId())) {
                expanded.add(review.reviewId());
            }
            render();
        });
        return toggle;
    }

    private void addOpenDetails(final JPanel card, final ReportedReview review) {
        card.add(Box.createVerticalStrut(DETAIL_GAP));
        card.add(left(Theme.label("Additional report details", LABEL_FONT_SIZE, Theme.MUTED)));
        int index = 1;
        for (final ReportedReview.AdditionalDetail detail : review.additionalDetails()) {
            card.add(left(Theme.label(index + ". " + detail.reason(), LABEL_FONT_SIZE, Theme.INK)));
            card.add(left(Theme.label("      " + QUOTE + detail.text() + QUOTE, LABEL_FONT_SIZE, Theme.MUTED)));
            index++;
        }
    }

    private JComponent createActions(final ReportedReview review) {
        final JButton dismiss = Theme.button("Dismiss Reports");
        final JButton remove = Theme.primary("Remove Review");
        dismiss.addActionListener(entryValue -> dismissReview(review));
        remove.addActionListener(entryValue -> removeReview(review));
        final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Theme.PAPER);
        actions.add(dismiss);
        actions.add(remove);
        return left(actions);
    }

    private void dismissReview(final ReportedReview review) {
        if (controller != null) {
            controller.dismiss(review.reviewId(), moderatorUsername);
        }
    }

    private void removeReview(final ReportedReview review) {
        if (controller != null) {
            controller.remove(review.reviewId(), moderatorUsername);
        }
    }

    private JPanel row(final JComponent left, final JComponent right) {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.PAPER);
        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    public void setOnBack(final Runnable onBack) {
        this.onBack = onBack;
    }

    public void setModeratorUsername(final String moderatorUsername) {
        this.moderatorUsername = moderatorUsername;
    }

    public void setController(final ModerateReviewsController controller) {
        this.controller = controller;
    }
}
