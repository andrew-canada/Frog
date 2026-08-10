package app;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.mongodb.MongoException;
import entity.Washroom;
import interface_adapter.view_reviews.WashroomListViewModel;
import views.MainView;

/** Loads the baseline data without blocking Swing's event-dispatch thread. */
final class ApplicationDataLoader {
    private static final int EARTH_RADIUS_METERS = 6_371_000;
    private static final double ORIGIN_LATITUDE = 43.6629;
    private static final double ORIGIN_LONGITUDE = -79.3957;

    private ApplicationDataLoader() {
    }

    static void loadAsync(final ApplicationInfrastructure infrastructure, final ApplicationModels models,
                          final ApplicationViews views, final boolean seedData, final Runnable loadModerator,
                          final Runnable onLoaded) {
        final Thread loader = new Thread(() -> {
            final LoadResult result = loadData(infrastructure, seedData, loadModerator);
            SwingUtilities.invokeLater(() -> complete(result, models, views, onLoaded));
        }, "FlushID initial data loader");
        loader.setDaemon(false);
        loader.start();
    }

    private static LoadResult loadData(final ApplicationInfrastructure infrastructure, final boolean seedData,
                                       final Runnable loadModerator) {
        LoadResult result;
        try {
            result = loadSuccessfully(infrastructure, seedData, loadModerator);
        }
        catch (final IllegalArgumentException | IllegalStateException | MongoException failure) {
            result = new LoadResult(List.of(), List.of(), failure);
        }
        return result;
    }

    private static LoadResult loadSuccessfully(final ApplicationInfrastructure infrastructure, final boolean seedData,
                                               final Runnable loadModerator) {
        if (seedData) {
            CampusStartup.seedInitialData(infrastructure.washrooms(), infrastructure.reviews(),
                infrastructure.reports(), ApplicationNames.values());
        }
        final List<Washroom> washrooms = infrastructure.washrooms().getByNames(ApplicationNames.values());
        final List<MainView.HeatmapData> heatmap = CampusStartup.heatmapData(washrooms, infrastructure.reports());
        loadModeratorSafely(loadModerator);
        return new LoadResult(washrooms, heatmap, null);
    }

    private static void loadModeratorSafely(final Runnable loadModerator) {
        try {
            loadModerator.run();
        }
        catch (final IllegalArgumentException | IllegalStateException | MongoException failure) {
            System.err.println("Moderator queue did not load: " + failure.getMessage());
        }
    }

    private static void complete(final LoadResult result, final ApplicationModels models,
                                 final ApplicationViews views, final Runnable onLoaded) {
        if (result.failure() == null) {
            applyLoaded(result, models, views);
        }
        else {
            applyFailure(models, views);
        }
        onLoaded.run();
    }

    private static void applyLoaded(final LoadResult result, final ApplicationModels models,
                                    final ApplicationViews views) {
        refreshMainWashrooms(result.washrooms(), models, views);
        views.primary().setHeatmapData(result.heatmap());
    }

    private static void applyFailure(final ApplicationModels models, final ApplicationViews views) {
        models.features().list().setState(new WashroomListViewModel.State(List.of(), "", "Alphabetical", false));
        JOptionPane.showMessageDialog(views.cards(),
            "Could not load the washroom map. Check the database connection and try again.", "FlushID",
            JOptionPane.ERROR_MESSAGE);
    }

    private static void refreshMainWashrooms(final List<Washroom> washrooms, final ApplicationModels models,
                                             final ApplicationViews views) {
        views.setWashrooms(washrooms);
        final double latitude = ORIGIN_LATITUDE;
        final double longitude = ORIGIN_LONGITUDE;
        final List<WashroomListViewModel.Item> items = washrooms.stream().map(washroom -> {
            return new WashroomListViewModel.Item(washroom.id(), washroom.building().name(), listDescription(washroom),
                washroom.reviewSummary().averageRating(),
                (int) Math.round(distance(latitude, longitude, washroom.building().latitude(),
                    washroom.building().longitude())), washroom.accessible());
        }).toList();
        final String selectedId;
        if (views.primary().selectedId().isBlank() && !items.isEmpty()) {
            selectedId = items.getFirst().id();
        }
        else {
            selectedId = views.primary().selectedId();
        }
        models.features().list().setState(new WashroomListViewModel.State(items, selectedId, "Sort by: Nearest",
            false));
    }

    static void refreshHeatmapAsync(final List<Washroom> washrooms, final ApplicationInfrastructure infrastructure,
                                    final ApplicationViews views) {
        CompletableFuture
            .supplyAsync(() -> heatmapData(washrooms, infrastructure))
            .thenAccept(values -> SwingUtilities.invokeLater(() -> views.primary().setHeatmapData(values)));
    }

    private static List<MainView.HeatmapData> heatmapData(final List<Washroom> washrooms,
                                                          final ApplicationInfrastructure infrastructure) {
        return CampusStartup.heatmapData(washrooms, infrastructure.reports());
    }

    static void updateWashroomListRating(final ApplicationModels models, final String washroomId,
                                         final double rating) {
        final WashroomListViewModel.State current = models.features().list().getState();
        final List<WashroomListViewModel.Item> updated = current.items().stream()
            .map(item -> updateItem(item, washroomId, rating))
            .toList();
        models.features().list().setState(new WashroomListViewModel.State(updated, current.selectedId(),
            current.sortLabel(), current.routeVisible()));
    }

    private static WashroomListViewModel.Item updateItem(final WashroomListViewModel.Item item,
                                                         final String washroomId, final double rating) {
        final WashroomListViewModel.Item result;
        if (item.id().equals(washroomId)) {
            result = new WashroomListViewModel.Item(item.id(), item.name(), item.description(), rating,
                item.distanceMeters(), item.accessible());
        }
        else {
            result = item;
        }
        return result;
    }

    private static String listDescription(final Washroom washroom) {
        final String name = washroom.name();
        final int separator = name.indexOf('|');
        final String description;
        if (separator >= 0) {
            description = name.substring(separator + 1);
        }
        else {
            description = name;
        }
        return description.replaceAll("(?i)\\bwashrooms?\\secondValue", "").replaceAll("\\s{2,}", " ").trim();
    }

    private static double distance(final double firstLatitude, final double firstLongitude,
                                   final double secondLatitude, final double secondLongitude) {
        final double x = Math.toRadians(secondLongitude - firstLongitude)
            * Math.cos(Math.toRadians((firstLatitude + secondLatitude) / 2));
        final double y = Math.toRadians(secondLatitude - firstLatitude);
        return Math.sqrt(x * x + y * y) * EARTH_RADIUS_METERS;
    }

    private record LoadResult(List<Washroom> washrooms, List<MainView.HeatmapData> heatmap, Exception failure) {
    }
}
