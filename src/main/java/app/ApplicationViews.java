package app;

import java.awt.CardLayout;
import java.util.List;

import javax.swing.JPanel;

import entity.Washroom;

/** View facade used by the application assembler. */
final class ApplicationViews {
    static final String MAIN = "main";
    static final String REVIEWS = "reviews";
    static final String LOGIN = "login";
    static final String STATUS = "status";
    static final String BUSYNESS = "busyness";
    static final String ACCOUNT = "account";
    static final String MODERATE = "moderate";

    private final CardLayout layout = new CardLayout();
    private final JPanel cards = new JPanel(layout);
    private final PrimaryViews primary;
    private final SecondaryViews secondary;
    private final DialogViews dialogs;
    private List<Washroom> displayedWashrooms = List.of();

    private ApplicationViews(final PrimaryViews primary, final SecondaryViews secondary,
                             final DialogViews dialogs) {
        this.primary = primary;
        this.secondary = secondary;
        this.dialogs = dialogs;
        cards.add(primary.main(), MAIN);
        cards.add(primary.reviews(), REVIEWS);
        cards.add(primary.login(), LOGIN);
    }

    static ApplicationViews create(final ApplicationInfrastructure infrastructure, final ApplicationModels models,
                                    final ApplicationControllers controllers) {
        final PrimaryViews primary = PrimaryViews.create(infrastructure, models, controllers);
        final SecondaryViews secondary = SecondaryViews.create(models, controllers);
        return new ApplicationViews(primary, secondary, DialogViews.create(controllers, models));
    }

    void addSecondaryCards() {
        cards.add(secondary.account(), ACCOUNT);
        cards.add(secondary.status(), STATUS);
        cards.add(secondary.busyness(), BUSYNESS);
        cards.add(secondary.moderate(), MODERATE);
    }

    JPanel cards() {
        return cards;
    }

    void show(final String name) {
        layout.show(cards, name);
    }

    PrimaryViews primary() {
        return primary;
    }

    SecondaryViews secondary() {
        return secondary;
    }

    DialogViews dialogs() {
        return dialogs;
    }

    void setWashrooms(final List<Washroom> washrooms) {
        displayedWashrooms = List.copyOf(washrooms);
        primary.setWashrooms(washrooms);
    }

    List<Washroom> washrooms() {
        return displayedWashrooms;
    }

}
