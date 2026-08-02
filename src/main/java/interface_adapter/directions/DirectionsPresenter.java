package interface_adapter.directions;

import javax.swing.SwingUtilities;

import use_case.directions.GetDirectionsOutputBoundary;
import use_case.directions.GetDirectionsOutputData;

public final class DirectionsPresenter implements GetDirectionsOutputBoundary {
    private final MapViewModel model;

    public DirectionsPresenter(MapViewModel model) {
        this.model = model;
    }

    @Override
    public void present(GetDirectionsOutputData d) {
        Runnable update = () -> model.setState(new MapViewModel.State(d.success(), d.routePoints(),
                d.success() ? d.distanceMeters() + " m" : "", d.success() ? Math.max(1, d.timeSeconds() / 60) + " min" : "", d.message()));
        if (SwingUtilities.isEventDispatchThread()) update.run();
        else SwingUtilities.invokeLater(update);
    }
}
