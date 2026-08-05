package interface_adapter.directions;

import use_case.directions.GetDirectionsInputBoundary;
import use_case.directions.GetDirectionsInputData;

public final class DirectionsController {
    private final GetDirectionsInputBoundary interactor;

    public DirectionsController(GetDirectionsInputBoundary i) {
        interactor = i;
    }

    public void execute(double lat, double lng, String id) {
        interactor.execute(new GetDirectionsInputData(lat, lng, id));
    }
}
