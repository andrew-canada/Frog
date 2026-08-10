package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import interface_adapter.view_reviews.WashroomListViewModel;

/** Builds the selectable cards shown in the washroom list. */
final class WashroomCardFactory {
    private static final int CARD_PADDING = 10;
    private static final int CARD_MAX_HEIGHT = 124;
    private static final int MARKER_RADIUS = 14;
    private static final int MARKER_DIAMETER = 28;
    private static final int SMALL_GAP = 4;
    private static final int DIRECTIONS_BUTTON_WIDTH = 94;
    private static final int REVIEWS_BUTTON_WIDTH = 78;

    private WashroomCardFactory() {
    }

    static JPanel create(final WashroomListViewModel.Item item, final String selectedId,
                         final Consumer<String> selectAction, final Consumer<String> reviewsAction,
                         final Consumer<String> directionsAction) {
        final boolean selected = item.id().equals(selectedId);
        final JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_MAX_HEIGHT));
        final Color backgroundColor;
        if (selected) {
            backgroundColor = Theme.PALE_BLUE;
        }
        else {
            backgroundColor = Theme.PAPER;
        }
        card.setBackground(backgroundColor);
        final Color borderColor;
        if (selected) {
            borderColor = Theme.BLUE;
        }
        else {
            borderColor = Theme.LINE;
        }
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor),
            Theme.pad(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING)));
        final Color nameColor;
        if (selected) {
            nameColor = Theme.BLUE;
        }
        else {
            nameColor = Theme.INK;
        }
        final JLabel name = Theme.label(item.name(), MARKER_RADIUS, nameColor);
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        card.add(name, BorderLayout.NORTH);
        final JPanel information = createInformation(item);
        card.add(information, BorderLayout.CENTER);
        final CardClickListener selectListener = new CardClickListener(() -> selectAction.accept(item.id()));
        card.addMouseListener(selectListener);
        name.addMouseListener(selectListener);
        information.addMouseListener(selectListener);
        for (final java.awt.Component component : information.getComponents()) {
            component.addMouseListener(selectListener);
        }
        card.add(createActions(item, selectAction, reviewsAction, directionsAction), BorderLayout.SOUTH);
        return card;
    }

    private static JPanel createInformation(final WashroomListViewModel.Item item) {
        final JPanel information = new JPanel();
        information.setLayout(new BoxLayout(information, BoxLayout.Y_AXIS));
        information.setOpaque(false);
        final JLabel description = Theme.label(item.description(), 12, Theme.INK);
        description.setFont(description.getFont().deriveFont(Font.BOLD));
        description.setAlignmentX(0.0f);
        final JLabel details = Theme.label(String.format("* %.1f  -  %d m away", item.rating(), item.distanceMeters()),
            12, Theme.MUTED);
        details.setAlignmentX(0.0f);
        information.add(description);
        information.add(Box.createVerticalStrut(SMALL_GAP));
        information.add(details);
        return information;
    }

    private static JPanel createActions(final WashroomListViewModel.Item item, final Consumer<String> selectAction,
                                        final Consumer<String> reviewsAction,
                                        final Consumer<String> directionsAction) {
        final JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);
        final JButton reviews = Theme.button("Reviews");
        final JButton directions = Theme.button("Directions");
        reviews.setPreferredSize(new Dimension(REVIEWS_BUTTON_WIDTH, MARKER_DIAMETER));
        directions.setPreferredSize(new Dimension(DIRECTIONS_BUTTON_WIDTH, MARKER_DIAMETER));
        reviews.addActionListener(event -> {
            selectAction.accept(item.id());
            reviewsAction.accept(item.id());
        });
        directions.addActionListener(event -> {
            selectAction.accept(item.id());
            directionsAction.accept(item.id());
        });
        actions.add(reviews);
        actions.add(directions);
        return actions;
    }
}
