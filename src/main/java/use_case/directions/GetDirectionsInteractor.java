package use_case.directions;

import java.util.List;

import entity.GeoPoint;
import entity.Route;
import entity.Washroom;
import use_case.port.RouteGateway;
import use_case.port.WashroomRepository;

public final class GetDirectionsInteractor implements GetDirectionsInputBoundary {
    private final WashroomRepository washrooms;
    private final RouteGateway routes;
    private final GetDirectionsOutputBoundary presenter;

    public GetDirectionsInteractor(final WashroomRepository washrooms, final RouteGateway routes,
                                   final GetDirectionsOutputBoundary presenter) {
        this.washrooms = washrooms;
        this.routes = routes;
        this.presenter = presenter;
    }

    @Override
    public void execute(final GetDirectionsInputData in) {
        final Washroom w = washrooms
            .getById(in.washroomId())
            .orElse(null);
        if (w == null) {
            presenter.present(new GetDirectionsOutputData(false, List.of(), 0, 0, "Washroom not found"));
            return;
        }
        try {
            final Route route = routes.getRoute(new GeoPoint(in.originLatitude(), in.originLongitude()), new GeoPoint(w
                .building()
                .latitude(), w
                .building()
                .longitude()));
            presenter.present(
                new GetDirectionsOutputData(true, route.points(), route.distanceMeters(), route.timeSeconds(),
                    "Route ready"));
        }
        catch (final RuntimeException failure) {
            final String message =
                failure.getMessage() == null ? "Directions are temporarily unavailable" : failure.getMessage();
            presenter.present(new GetDirectionsOutputData(false, List.of(), 0, 0, message));
        }
    }
}
