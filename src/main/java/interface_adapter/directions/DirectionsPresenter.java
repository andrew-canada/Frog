package interface_adapter.directions;

import interface_adapter.common.UiDispatcher;
import use_case.directions.GetDirectionsOutputBoundary;
import use_case.directions.GetDirectionsOutputData;

public final class DirectionsPresenter implements GetDirectionsOutputBoundary {
    private final MapViewModel model;
    private final UiDispatcher ui;

    public DirectionsPresenter(MapViewModel model, UiDispatcher ui) {
        this.model = model;
        this.ui = ui;
    }

    @Override
    public void present(GetDirectionsOutputData d) {
        Runnable update = () -> model.setState(new MapViewModel.State(d.success(), d.routePoints(),
                d.success() ? d.distanceMeters() + " m" : "", d.success() ? Math.max(1, d.timeSeconds() / 60) + " min" : "", d.message()));
        ui.dispatch(update);
    }
}
