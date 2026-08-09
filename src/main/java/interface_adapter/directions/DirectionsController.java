package interface_adapter.directions;

import use_case.directions.GetDirectionsInputBoundary;
import use_case.directions.GetDirectionsInputData;

public final class DirectionsController {
    private final GetDirectionsInputBoundary interactor;

    public DirectionsController(final GetDirectionsInputBoundary i) {
        interactor = i;
    }

    public void execute(final double lat, final double lng, final String id) {
        interactor.execute(new GetDirectionsInputData(lat, lng, id));
    }
}
