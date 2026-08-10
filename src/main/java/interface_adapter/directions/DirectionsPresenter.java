package interface_adapter.directions;

import interface_adapter.common.UiDispatcher;
import use_case.directions.GetDirectionsOutputBoundary;
import use_case.directions.GetDirectionsOutputData;

public final class DirectionsPresenter implements GetDirectionsOutputBoundary {
    private static final int SECONDS_PER_MINUTE = 60;
    private final MapViewModel model;
    private final UiDispatcher userInterface;

    public DirectionsPresenter(final MapViewModel model, final UiDispatcher userInterface) {
        this.model = model;
        this.userInterface = userInterface;
    }

    @Override
    public void present(final GetDirectionsOutputData d) {
        userInterface.dispatch(() -> updateMapState(d));
    }

    private void updateMapState(final GetDirectionsOutputData data) {
        final String distanceLabel;
        final String timeLabel;
        if (data.success()) {
            distanceLabel = data.distanceMeters() + " m";
            timeLabel = Math.max(1, data.timeSeconds() / SECONDS_PER_MINUTE) + " min";
        }
        else {
            distanceLabel = "";
            timeLabel = "";
        }
        model.setState(new MapViewModel.State(data.success(), data.routePoints(), distanceLabel, timeLabel,
            data.message()));
    }
}
