package view;

import interface_adapter.sort_reviews.SortReviewsController;
import interface_adapter.view_reviews.ReviewsViewModel;
import use_case.view_reviews.ViewReviewsOutputData;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;


public final class ReadReviewsView extends JPanel {
    private static final Color HELPFUL_GREEN = new Color(0x4C, 0xAF, 0x50);
    private final JLabel title = Theme.title("Reviews"), subtitle = Theme.label("", 12, Theme.INK), summary = Theme.label("", 13, Theme.MUTED);
    private final JPanel reviews = new JPanel();
    private SortReviewsController sortReviewsController;
    private Runnable onBack = () -> {
    }, onWrite = () -> {
    };
    private Consumer<String> onHelpful = id -> {
    }, onReport = id -> {
    };

    public ReadReviewsView(ReviewsViewModel reviewsViewModel) {
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.BOLD));
        add(header(reviewsViewModel), BorderLayout.NORTH);

        reviews.setLayout(new BoxLayout(reviews, BoxLayout.Y_AXIS));
        reviews.setBackground(Theme.PAPER);
        JScrollPane scroll = new JScrollPane(reviews);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
        scroll.getVerticalScrollBar().setValue(0);
        reviewsViewModel.addPropertyChangeListener(e -> render(reviewsViewModel.getState()));
    }

    private JComponent header(ReviewsViewModel s) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.PAPER);
        outer.setBorder(Theme.pad(18, 24, 14, 24));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(subtitle);
        text.add(Box.createVerticalStrut(12));
        text.add(summary);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton write = Theme.primary("+ Write a review"), back = Theme.button("← Back to map");
        ReviewSortDropdownControl reviewSortDropdownControl = new ReviewSortDropdownControl();
        write.addActionListener(e -> onWrite.run());
        back.addActionListener(e -> onBack.run());
        reviewSortDropdownControl.addActionListener(e ->
                sortReviewsController.execute(
                        reviewSortDropdownControl.getSelectedItem().toString(),
                        s.getState().washroomId()
                ));
        text.add(reviewSortDropdownControl);
        outer.add(text);
        buttons.add(write);
        buttons.add(back);
        outer.add(buttons, BorderLayout.EAST);
        return outer;
    }

    public void render(ReviewsViewModel.State s) {
        title.setText(s.name());
        subtitle.setText(s.subtitle());
        summary.setText(String.format("★ %.1f  ·  Based on %d reviews", s.rating(), s.reviewCount()));
        reviews.removeAll();
        renderReviews(s.reviews());
        reviews.revalidate();
        reviews.repaint();
    }

    public void renderReviews(List<ViewReviewsOutputData.ReviewDisplay> s){
        for (ViewReviewsOutputData.ReviewDisplay r : s) {
            JPanel card = new JPanel(new BorderLayout(10, 10));
            card.setBackground(Theme.PAPER);
            card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE), Theme.pad(18, 24, 18, 24)));
            JLabel stars = Theme.label(String.format("★ %.1f", r.rating()), 14, Theme.INK);
            stars.setFont(stars.getFont().deriveFont(Font.BOLD));
            card.add(stars, BorderLayout.WEST);
            JTextArea body = new JTextArea(r.comment());
            body.setEditable(false);
            body.setLineWrap(true);
            body.setWrapStyleWord(true);
            body.setFont(body.getFont().deriveFont(14f));
            body.setBackground(Theme.PAPER);
            card.add(body);
            JPanel meta = new JPanel();
            meta.setOpaque(false);
            meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
            meta.add(Theme.label(r.date().format(DateTimeFormatter.ofPattern("MMM d, yyyy")), 12, Theme.MUTED));
            meta.add(Box.createVerticalStrut(12));
            JButton helpful = Theme.button("Helpful · " + r.helpfulCount());
            if (r.votedByCurrentUser()) {
                // Turn off the look-and-feel's own button-face painting so our green background
                // actually shows; otherwise the L&F paints its grey face over setBackground().
                helpful.setContentAreaFilled(false);
                helpful.setOpaque(true);
                helpful.setBackground(HELPFUL_GREEN);
                helpful.setForeground(Color.WHITE);
            }
            helpful.addActionListener(e -> onHelpful.accept(r.reviewId()));
            meta.add(helpful);
            meta.add(Box.createVerticalStrut(6));
            JButton report = Theme.button(r.reportedByCurrentUser() ? "Reported" : "Report");
            if (r.reportedByCurrentUser()) {
                // Inert "reported" chip: no action listener (so it can't report again), but kept
                // enabled so the accent colors render - a disabled button greys its text.
                report.setContentAreaFilled(false);
                report.setOpaque(true);
                report.setBackground(Theme.BERRY);
                report.setForeground(Color.WHITE);
                report.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.BERRY.darker()), Theme.pad(8, 14, 8, 14)));
                report.setFocusable(false);
            } else {
                report.addActionListener(e -> onReport.accept(r.reviewId()));
            }
            meta.add(report);
            card.add(meta, BorderLayout.EAST);
            reviews.add(card);
        }
    }

    public void setOnBack(Runnable r) {
        onBack = r;
    }

    public void setOnWrite(Runnable r) {
        onWrite = r;
    }

    public void setOnHelpful(Consumer<String> c) {
        onHelpful = c;
    }

    public void setOnReport(Consumer<String> c) {
        onReport = c;
    }

    public void setSortReviewsController(SortReviewsController c){sortReviewsController = c;}
}
