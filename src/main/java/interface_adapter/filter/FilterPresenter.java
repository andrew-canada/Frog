package interface_adapter.filter;

import java.util.ArrayList;
import java.util.List;

import interface_adapter.common.UiDispatcher;
import interface_adapter.view_reviews.WashroomListViewModel;
import use_case.filter.FilterOutputBoundary;
import use_case.filter.FilterOutputData;

public class FilterPresenter implements FilterOutputBoundary {
    private static final int MAGIC_6_371_000 = 6_371_000;
    private final WashroomListViewModel listModel;
    private final FilterViewModel filterModel;
    private final UiDispatcher userInterface;

    public FilterPresenter(final FilterViewModel filterViewModel, final WashroomListViewModel listModel,
                           final UiDispatcher userInterface) {

        this.filterModel = filterViewModel;
        this.listModel = listModel;
        this.userInterface = userInterface;
    }

    private static double distance(final double firstValue, final double secondValue, final double thirdValue,
        final double fourthValue) {
        final double x = Math.toRadians(fourthValue - secondValue) * Math.cos(Math.toRadians((firstValue
            + thirdValue) / 2));
        final double y = Math.toRadians(thirdValue - firstValue);
        return Math.sqrt(x * x + y * y) * MAGIC_6_371_000;
    }

    /**
     * Keeps filtered cards consistent with the initial washroom-list display.
     * @param washroomName parameter value.
     * @return the operation result.
     */
    private static String listDescription(final String washroomName) {
        final int separator = washroomName.indexOf('|');
        final String description;
        if (separator >= 0) {
            description = washroomName.substring(separator + 1);
        }
        else {
            description = washroomName;
        }
        return description
            .replaceAll("(?i)\\bwashrooms?\\secondValue", "")
            .replaceAll("\\s{2,}", " ")
            .trim();
    }

    @Override
    public void present(final FilterOutputData outputData) {
        final List<WashroomListViewModel.Item> items = outputData
            .washrooms()
            .stream()
            .map(washroom -> item(outputData, washroom))
            .toList();
        final Runnable update = () -> {
            listModel.setState(new WashroomListViewModel.State(items, null, "Sort by: Nearest", false));
            filterModel.setState(new FilterViewModel.State(true, outputData.washrooms(), ""));
        };
        userInterface.dispatch(update);

    }

    @Override
    public void presentError(final String message) {
        final Runnable update =
            () -> {
                filterModel.setState(new FilterViewModel.State(false, new ArrayList<>(), message));
            };
        userInterface.dispatch(update);
    }

    private static WashroomListViewModel.Item item(final FilterOutputData outputData,
                                                    final entity.Washroom washroom) {
        return new WashroomListViewModel.Item(washroom.id(), washroom.building().name(),
            listDescription(washroom.name()),
            washroom.reviewSummary().averageRating(), (int) Math.round(distance(outputData.latitude(),
                outputData.longitude(), washroom.building().latitude(), washroom.building().longitude())),
            washroom.accessible());
    }
}
