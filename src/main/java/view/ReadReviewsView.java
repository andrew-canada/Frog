package view;

import interface_adapter.view_reviews.ReviewsViewModel;
import use_case.view_reviews.ViewReviewsOutputData;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public final class ReadReviewsView extends JPanel{
    private final JLabel title=Theme.title("Reviews"),subtitle=Theme.label("",12,Theme.MUTED),summary=Theme.label("",13,Theme.MUTED);
    private final JPanel reviews=new JPanel();private Runnable onBack=()->{},onWrite=()->{};
    public ReadReviewsView(ReviewsViewModel model){setLayout(new BorderLayout());setBackground(Theme.PAPER);add(header(),BorderLayout.NORTH);
        reviews.setLayout(new BoxLayout(reviews,BoxLayout.Y_AXIS));reviews.setBackground(Theme.PAPER);JScrollPane scroll=new JScrollPane(reviews);scroll.setBorder(null);add(scroll,BorderLayout.CENTER);
        model.addPropertyChangeListener(e->render(model.getState()));}
    private JComponent header(){JPanel outer=new JPanel(new BorderLayout());outer.setBackground(Theme.PAPER);outer.setBorder(Theme.pad(18,24,14,24));JPanel text=new JPanel();text.setOpaque(false);text.setLayout(new BoxLayout(text,BoxLayout.Y_AXIS));text.add(title);text.add(subtitle);text.add(Box.createVerticalStrut(12));text.add(summary);outer.add(text);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));buttons.setOpaque(false);JButton write=Theme.primary("+ Write a review"),back=Theme.button("← Back to map");write.addActionListener(e->onWrite.run());back.addActionListener(e->onBack.run());buttons.add(write);buttons.add(back);outer.add(buttons,BorderLayout.EAST);return outer;}
    public void render(ReviewsViewModel.State s){title.setText(s.name());subtitle.setText(s.subtitle());summary.setText(String.format("★ %.1f  ·  Based on %d reviews  ·  %d toilets  ·  %d sinks",s.rating(),s.reviewCount(),s.toilets(),s.sinks()));reviews.removeAll();
        for(ViewReviewsOutputData.ReviewDisplay r:s.reviews()){JPanel card=new JPanel(new BorderLayout(10,10));card.setBackground(Theme.PAPER);card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,Theme.LINE),Theme.pad(18,24,18,24)));
            JLabel stars=Theme.label(String.format("★ %.1f",r.rating()),14,Theme.INK);stars.setFont(stars.getFont().deriveFont(Font.BOLD));card.add(stars,BorderLayout.WEST);JTextArea body=new JTextArea(r.comment());body.setEditable(false);body.setLineWrap(true);body.setWrapStyleWord(true);body.setFont(body.getFont().deriveFont(14f));body.setBackground(Theme.PAPER);card.add(body);
            JPanel meta=new JPanel();meta.setOpaque(false);meta.setLayout(new BoxLayout(meta,BoxLayout.Y_AXIS));meta.add(Theme.label(r.date().format(DateTimeFormatter.ofPattern("MMM d, yyyy")),12,Theme.MUTED));meta.add(Box.createVerticalStrut(12));meta.add(Theme.button("Helpful · "+r.helpfulCount()));card.add(meta,BorderLayout.EAST);reviews.add(card);}reviews.revalidate();reviews.repaint();}
    public void setOnBack(Runnable r){onBack=r;}public void setOnWrite(Runnable r){onWrite=r;}
}
