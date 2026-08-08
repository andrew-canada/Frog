package interface_adapter.sort_washrooms;

import entity.Washroom;
import interface_adapter.filter.FilterViewModel;
import interface_adapter.view_reviews.WashroomListViewModel;
import use_case.filter.FilterOutputBoundary;
import use_case.sort_washrooms.SortWashroomsOutputBoundary;
import use_case.sort_washrooms.SortWashroomsOutputData;

import javax.swing.*;
import java.util.List;

public class SortWashroomPresenter implements SortWashroomsOutputBoundary {
    private final SortWashroomViewModel viewModel;

    public SortWashroomPresenter(SortWashroomViewModel viewModel){
        this.viewModel = viewModel;
    }

    private static double distance(double a, double b, double c, double d) {
        double x = Math.toRadians(d - b) * Math.cos(Math.toRadians((a + c) / 2));
        double y = Math.toRadians(c - a);
        return Math.sqrt(x * x + y * y) * 6_371_000;
    }

    @Override
    public void present(SortWashroomsOutputData outputData) {
        List<WashroomListViewModel.Item> items = outputData.washrooms().stream().map(
                washroom -> new WashroomListViewModel.Item(
                        washroom.id(),
                        washroom.building().name(),
                        washroom.locationDescription(),
                        washroom.reviewSummary().averageRating(),
                        (int) Math.round(distance(
                                outputData.latitude(),
                                outputData.longitude(),
                                washroom.building().latitude(),
                                washroom.building().longitude())),
                        washroom.accessible())).toList();
        Runnable update = () -> {
            viewModel.setState(new SortWashroomViewModel.State(true, items));
        };
        if (SwingUtilities.isEventDispatchThread()) update.run();
        else SwingUtilities.invokeLater(update);
    }
}
