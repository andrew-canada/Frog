package interface_adapter.directions;

import use_case.directions.GetDirectionsInputBoundary;
import use_case.directions.GetDirectionsInputData;

public final class DirectionsController {
    private final GetDirectionsInputBoundary interactor;

    public DirectionsController(final GetDirectionsInputBoundary indexValue) {
        interactor = indexValue;
    }

    /**
     * Performs this operation.
     *
     * @param lat parameter value.
     *
     * @param lng parameter value.
     *
     * @param id parameter value.
     */
    public void execute(final double lat, final double lng, final String id) {
        interactor.execute(new GetDirectionsInputData(lat, lng, id));
    }
}
