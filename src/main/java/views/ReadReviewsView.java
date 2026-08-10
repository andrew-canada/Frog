package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import interface_adapter.sort_reviews.SortReviewsController;
import interface_adapter.view_reviews.ReviewsViewModel;
import use_case.view_reviews.ViewReviewsOutputData;

public final class ReadReviewsView extends JPanel {
    private static final int BODY_FONT_SIZE = 14;
    private static final int SMALL_GAP = 8;
    private static final int DETAIL_GAP = 6;
    private static final int COUNT_FONT_SIZE = 12;
    private static final float BODY_FONT_SIZE_FLOAT = 14f;
    private static final int CARD_HORIZONTAL_PADDING = 24;
    private static final int CARD_VERTICAL_PADDING = 18;
    private static final char STAR = 9733;
    private static final char MIDDLE_DOT = 183;
    private static final Color HELPFUL_GREEN = new Color(0x4C, 0xAF, 0x50);
    private final JLabel title = Theme.title("Reviews");
    private final JLabel subtitle = Theme.label("", 12, Theme.INK);
    private final JLabel summary = Theme.label("", 13, Theme.MUTED);
    private final JPanel reviews = new JPanel();
    private SortReviewsController sortReviewsController;
    private Runnable onBack = () -> {
    };
    private Runnable onWrite = () -> {
    };
    private Consumer<String> onHelpful = id -> {
    };
    private Consumer<String> onReport = id -> {
    };

    public ReadReviewsView(final ReviewsViewModel reviewsViewModel) {
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        subtitle.setFont(subtitle
            .getFont()
            .deriveFont(Font.BOLD));
        add(header(reviewsViewModel), BorderLayout.NORTH);

        reviews.setLayout(new BoxLayout(reviews, BoxLayout.Y_AXIS));
        reviews.setBackground(Theme.PAPER);
        final JScrollPane scroll = new JScrollPane(reviews);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
        scroll
            .getVerticalScrollBar()
            .setValue(0);
        reviewsViewModel.addPropertyChangeListener(entryValue -> {
            render(reviewsViewModel.getState());
        });
    }

    private JComponent header(final ReviewsViewModel parameterValue) {
        final JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.PAPER);
        outer.setBorder(Theme.pad(CARD_VERTICAL_PADDING, CARD_HORIZONTAL_PADDING, BODY_FONT_SIZE,
            CARD_HORIZONTAL_PADDING));
        final JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(subtitle);
        text.add(Box.createVerticalStrut(COUNT_FONT_SIZE));
        text.add(summary);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        final JButton write = Theme.primary("+ Write a review");
        final JButton back = Theme.button("<- Back to map");
        final ReviewSortDropdownControl reviewSortDropdownControl = new ReviewSortDropdownControl();
        write.addActionListener(entryValue -> {
            onWrite.run();
        });
        back.addActionListener(entryValue -> {
            onBack.run();
        });
        reviewSortDropdownControl.addActionListener(entryValue -> {
            sortReviewsController.execute(reviewSortDropdownControl
                .getSelectedItem()
                .toString(), parameterValue
                .getState()
                .washroomId());
        });

        outer.add(text);
        buttons.add(reviewSortDropdownControl);
        buttons.add(write);
        buttons.add(back);
        outer.add(buttons, BorderLayout.EAST);
        return outer;
    }

    private void render(final ReviewsViewModel.State parameterValue) {
        title.setText(parameterValue.name());
        subtitle.setText(parameterValue.subtitle());
        summary.setText(String.format("%s %.1f  %s  Based on %d reviews", Character.toString(STAR),
            parameterValue.rating(), Character.toString(MIDDLE_DOT), parameterValue.reviewCount()));
        reviews.removeAll();
        renderReviews(parameterValue.reviews());
        reviews.revalidate();
        reviews.repaint();
    }

    private void renderReviews(final List<ViewReviewsOutputData.ReviewDisplay> parameterValue) {
        for (final ViewReviewsOutputData.ReviewDisplay reviewValue : parameterValue) {
            reviews.add(reviewCard(reviewValue));
        }
    }

    private JPanel reviewCard(final ViewReviewsOutputData.ReviewDisplay reviewValue) {
        final JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Theme.PAPER);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE),
            Theme.pad(CARD_VERTICAL_PADDING, CARD_HORIZONTAL_PADDING, CARD_VERTICAL_PADDING, CARD_HORIZONTAL_PADDING)));
        card.add(reviewContent(reviewValue), BorderLayout.CENTER);
        card.add(reviewActions(reviewValue), BorderLayout.EAST);
        return card;
    }

    private static JComponent reviewContent(final ViewReviewsOutputData.ReviewDisplay reviewValue) {
        final JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        final JLabel stars = Theme.label(String.format("* %.1f", reviewValue.rating()), BODY_FONT_SIZE, Theme.INK);
        stars.setFont(stars.getFont().deriveFont(Font.BOLD));
        content.add(stars, BorderLayout.WEST);
        final JTextArea body = new JTextArea(reviewValue.comment());
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(body.getFont().deriveFont(BODY_FONT_SIZE_FLOAT));
        body.setBackground(Theme.PAPER);
        content.add(body, BorderLayout.CENTER);
        return content;
    }

    private JPanel reviewActions(final ViewReviewsOutputData.ReviewDisplay reviewValue) {
        final JPanel meta = new JPanel();
        meta.setOpaque(false);
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
        meta.add(Theme.label(reviewValue.date().format(DateTimeFormatter.ofPattern("MMM d, yyyy")), COUNT_FONT_SIZE,
            Theme.MUTED));
        meta.add(Box.createVerticalStrut(COUNT_FONT_SIZE));
        meta.add(helpfulButton(reviewValue));
        meta.add(Box.createVerticalStrut(DETAIL_GAP));
        meta.add(reportButton(reviewValue));
        return meta;
    }

    private JButton helpfulButton(final ViewReviewsOutputData.ReviewDisplay reviewValue) {
        final JButton helpful = Theme.button("Helpful  -  " + reviewValue.helpfulCount());
        if (reviewValue.votedByCurrentUser()) {
            helpful.setContentAreaFilled(false);
            helpful.setOpaque(true);
            helpful.setBackground(HELPFUL_GREEN);
            helpful.setForeground(Color.WHITE);
        }
        helpful.addActionListener(entryValue -> onHelpful.accept(reviewValue.reviewId()));
        return helpful;
    }

    private JButton reportButton(final ViewReviewsOutputData.ReviewDisplay reviewValue) {
        final JButton report;
        if (reviewValue.reportedByCurrentUser()) {
            report = reportedButton();
        }
        else {
            report = Theme.button("Report");
            report.addActionListener(entryValue -> onReport.accept(reviewValue.reviewId()));
        }
        return report;
    }

    private static JButton reportedButton() {
        final JButton report = Theme.button("Reported");
        report.setContentAreaFilled(false);
        report.setOpaque(true);
        report.setBackground(Theme.BERRY);
        report.setForeground(Color.WHITE);
        report.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BERRY.darker()),
            Theme.pad(SMALL_GAP, BODY_FONT_SIZE, SMALL_GAP, BODY_FONT_SIZE)));
        report.setFocusable(false);
        return report;
    }

    public void setOnBack(final Runnable reviewValue) {
        onBack = reviewValue;
    }

    public void setOnWrite(final Runnable reviewValue) {
        onWrite = reviewValue;
    }

    public void setOnHelpful(final Consumer<String> thirdValue) {
        onHelpful = thirdValue;
    }

    public void setOnReport(final Consumer<String> thirdValue) {
        onReport = thirdValue;
    }

    public void setSortReviewsController(final SortReviewsController thirdValue) {
        sortReviewsController = thirdValue;
    }
}
