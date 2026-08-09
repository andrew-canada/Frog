package interface_adapter.sort_washrooms;

import interface_adapter.common.UiDispatcher;
import interface_adapter.view_reviews.WashroomListViewModel;
import use_case.sort_washrooms.SortWashroomsOutputBoundary;
import use_case.sort_washrooms.SortWashroomsOutputData;

import java.util.List;

public class SortWashroomPresenter implements SortWashroomsOutputBoundary {
    private final WashroomListViewModel listModel;
    private final SortWashroomViewModel sortModel;
    private final UiDispatcher ui;

    public SortWashroomPresenter(WashroomListViewModel listModel,
                                 SortWashroomViewModel sortModel, UiDispatcher ui) {
        this.listModel = listModel;
        this.sortModel = sortModel;
        this.ui = ui;
    }


    private static double distance(double a, double b, double c, double d) {
        double x = Math.toRadians(d - b) * Math.cos(Math.toRadians((a + c) / 2));
        double y = Math.toRadians(c - a);
        return Math.sqrt(x * x + y * y) * 6_371_000;
    }

    /**
     * Keeps filtered cards consistent with the initial washroom-list display.
     */
    private static String listDescription(String washroomName) {
        int separator = washroomName.indexOf('|');
        String description = separator >= 0 ? washroomName.substring(separator + 1) : washroomName;
        return description.replaceAll("(?i)\\bwashrooms?\\b", "")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    @Override
    public void present(SortWashroomsOutputData outputData) {
        List<WashroomListViewModel.Item> items = outputData.washrooms().stream().map(
                washroom -> new WashroomListViewModel.Item(
                        washroom.id(),
                        washroom.building().name(),
                        listDescription(washroom.name()),
                        washroom.reviewSummary().averageRating(),
                        (int) Math.round(distance(
                                outputData.latitude(),
                                outputData.longitude(),
                                washroom.building().latitude(),
                                washroom.building().longitude())),
                        washroom.accessible())).toList();
        Runnable update = () -> {
            listModel.setState(new WashroomListViewModel.State(items, null, "Sort by: Nearest", false));
            sortModel.setState(new
                    interface_adapter.sort_washrooms.SortWashroomViewModel.State(true, outputData.washrooms()));
        };
        ui.dispatch(update);

    }
}
