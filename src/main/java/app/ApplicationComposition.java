package app;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/** Assembles the application without exposing concrete wiring through AppBuilder. */
final class ApplicationComposition {
    private static final int WINDOW_HEIGHT = 700;
    private static final int WINDOW_WIDTH = 1060;
    private static final int MIN_WINDOW_HEIGHT = 600;
    private static final int MIN_WINDOW_WIDTH = 900;

    private ApplicationComposition() {
    }

    static JFrame build(final boolean seedData, final Consumer<JFrame> onLoaded) {
        final ApplicationInfrastructure infrastructure = ApplicationInfrastructure.create();
        final ApplicationModels models = new ApplicationModels();
        final ApplicationControllers controllers = ApplicationControllers.create(infrastructure, models);
        final ApplicationViews views = ApplicationViews.create(infrastructure, models, controllers);
        final JFrame frame = createFrame(infrastructure);
        views.addSecondaryCards();
        frame.setContentPane(views.cards());
        views.show(ApplicationViews.LOGIN);
        wireMain(infrastructure, models, controllers, views);
        wireReviews(infrastructure, models, controllers, views, frame);
        wireSecondary(models, controllers, views, frame);
        wireModels(infrastructure, models, views);
        ApplicationDataLoader.loadAsync(infrastructure, models, views, seedData, controllers::loadModeration,
            () -> onLoaded.accept(frame));
        return frame;
    }

    private static JFrame createFrame(final ApplicationInfrastructure infrastructure) {
        final JFrame frame = new JFrame("FlushID - U of T washroom finder");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                infrastructure.connection().close();
            }
        });
        return frame;
    }

    private static void wireMain(final ApplicationInfrastructure infrastructure, final ApplicationModels models,
                                 final ApplicationControllers controllers, final ApplicationViews views) {
        views.primary().setMainOnReviews(id -> {
            controllers.viewReviews(id, models.username());
            views.show(ApplicationViews.REVIEWS);
        });
        views.primary().setMainOnDirections(id -> {
            views.primary().showRouting();
            controllers.requestDirections(views.primary().latitude(), views.primary().longitude(), id);
        });
        views.primary().setMainOnLogin(() -> views.show(ApplicationViews.LOGIN));
        views.primary().setMainOnLogout(() -> views.show(ApplicationViews.LOGIN));
        views.primary().setMainOnAccount(() -> views.show(ApplicationViews.ACCOUNT));
        views.primary().setMainOnReport(() -> openStatus(infrastructure, views));
        views.primary().setMainOnBusyness(() -> openBusyness(infrastructure, controllers, views));
        views.primary().setMainOnModerator(() -> {
            views.secondary().setModerateUsername(models.username());
            controllers.loadModeration();
            views.show(ApplicationViews.MODERATE);
        });
    }

    private static void openStatus(final ApplicationInfrastructure infrastructure, final ApplicationViews views) {
        infrastructure.selected(views.primary().selectedId()).ifPresentOrElse(washroom -> {
            views.secondary().setStatusWashroomName(washroom.name());
            views.show(ApplicationViews.STATUS);
        }, () -> noWashroom(views));
    }

    private static void openBusyness(final ApplicationInfrastructure infrastructure,
                                     final ApplicationControllers controllers, final ApplicationViews views) {
        infrastructure.selected(views.primary().selectedId()).ifPresentOrElse(washroom -> {
            views.secondary().setBusynessLocationName(washroom.building().name());
            controllers.loadBusyness(washroom.id(), washroom.building().code(), DayOfWeek.from(LocalDate.now()));
            views.show(ApplicationViews.BUSYNESS);
        }, () -> noWashroom(views));
    }

    private static void wireReviews(final ApplicationInfrastructure infrastructure, final ApplicationModels models,
                                    final ApplicationControllers controllers, final ApplicationViews views,
                                    final JFrame frame) {
        final Runnable showMain = () -> views.show(ApplicationViews.MAIN);
        views.primary().setReviewsOnBack(showMain);
        views.primary().setReviewsOnWrite(() -> openWriteReview(infrastructure, models, controllers, views, frame));
        views.primary().setReviewsOnHelpful(id -> {
            controllers.toggleHelpful(id, models.username());
            models.reviews().reviews().toggleHelpfulVote(id);
        });
        views.primary().setReviewsOnReport(id -> openReportReview(models, controllers, views, frame, id));
        views.primary().setLoginOnBack(showMain);
        views.primary().setLoginOnSignup(() -> views.dialogs().showSignup(frame));
    }

    private static void openWriteReview(final ApplicationInfrastructure infrastructure, final ApplicationModels models,
                                        final ApplicationControllers controllers, final ApplicationViews views,
                                        final JFrame frame) {
        infrastructure.selected(views.primary().selectedId()).ifPresentOrElse(washroom -> {
            openWriteReviewDialog(models, controllers, views, frame, washroom.id(), washroom.name());
        }, () -> {
            noWashroom(views);
        });
    }

    private static void openWriteReviewDialog(final ApplicationModels models, final ApplicationControllers controllers,
                                              final ApplicationViews views, final JFrame frame,
                                              final String washroomId, final String washroomName) {
        final String username;
        if (models.loggedIn()) {
            username = models.username();
        }
        else {
            username = "Anonymous";
        }
        views.dialogs().showWriteReview(frame, washroomId, washroomName, username,
            () -> updateAfterWrite(models, controllers, washroomId));
    }

    private static void updateAfterWrite(final ApplicationModels models, final ApplicationControllers controllers,
                                         final String washroomId) {
        controllers.viewReviews(washroomId, models.username());
        ApplicationDataLoader.updateWashroomListRating(models, washroomId,
            models.reviews().reviews().getState().rating());
    }

    private static void openReportReview(final ApplicationModels models, final ApplicationControllers controllers,
                                         final ApplicationViews views, final JFrame frame, final String reviewId) {
        views.dialogs().showReportReview(frame, reviewId, models.username());
        controllers.viewReviews(models.reviews().reviews().getState().washroomId(), models.username());
        controllers.loadModeration();
    }

    private static void wireSecondary(final ApplicationModels models,
                                      final ApplicationControllers controllers, final ApplicationViews views,
                                      final JFrame frame) {
        final Runnable showMain = () -> views.show(ApplicationViews.MAIN);
        views.secondary().setStatusOnBack(showMain);
        views.secondary().setBusynessOnBack(showMain);
        views.secondary().setAccountOnBack(showMain);
        views.secondary().setModerateOnBack(showMain);
        views.secondary().setAccountOnViewPlan(plan -> views.dialogs().showPlan(frame, plan));
        views.secondary().setStatusOnSubmit(() -> submitStatus(models, controllers, views));
    }

    private static void submitStatus(final ApplicationModels models, final ApplicationControllers controllers,
                                     final ApplicationViews views) {
        if (!views.primary().selectedId().isBlank()) {
            final String username;
            if (models.loggedIn()) {
                username = models.username();
            }
            else {
                username = null;
            }
            controllers.submitStatus(views.primary().selectedId(), views.secondary().statusBusyness(),
                views.secondary().statusCleanliness(), views.secondary().statusIssue(),
                username);
        }
    }

    private static void wireModels(final ApplicationInfrastructure infrastructure, final ApplicationModels models,
                                   final ApplicationViews views) {
        models.reviews().moderate().addPropertyChangeListener(event -> updateModeratorCount(models, views));
        models.auth().loggedIn().addPropertyChangeListener(event -> {
            views.primary().setModerator(models.auth().loggedIn().getState().moderator());
        });
        models.features().status().addPropertyChangeListener(event -> {
            if (models.features().status().getState().success()) {
                ApplicationDataLoader.refreshHeatmapAsync(views.washrooms(), infrastructure, views);
            }
        });
    }

    private static void updateModeratorCount(final ApplicationModels models, final ApplicationViews views) {
        final Runnable update = () -> {
            views.primary().setModeratorReportCount(models.reviews().moderate().getState().reportedReviews().size());
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        }
        else {
            SwingUtilities.invokeLater(update);
        }
    }

    private static void noWashroom(final ApplicationViews views) {
        javax.swing.JOptionPane.showMessageDialog(views.cards(),
            "Select a washroom from the list or map before continuing.", "Select a washroom",
            javax.swing.JOptionPane.WARNING_MESSAGE);
    }
}
