package use_case.directions;

import entity.GeoPoint;
import entity.Route;
import entity.Washroom;
import use_case.gateway.RouteDataAccessInterface;
import use_case.gateway.WashroomDataAccessInterface;
import java.util.List;

public final class GetDirectionsInteractor implements GetDirectionsInputBoundary {
    private final WashroomDataAccessInterface washrooms; private final RouteDataAccessInterface routes;
    private final GetDirectionsOutputBoundary presenter;
    public GetDirectionsInteractor(WashroomDataAccessInterface washrooms, RouteDataAccessInterface routes,
                                   GetDirectionsOutputBoundary presenter) {
        this.washrooms=washrooms; this.routes=routes; this.presenter=presenter;
    }
    @Override public void execute(GetDirectionsInputData in) {
        Washroom w=washrooms.getById(in.washroomId()).orElse(null);
        if (w==null) { presenter.present(new GetDirectionsOutputData(false,List.of(),0,0,"Washroom not found")); return; }
        try {
            Route route=routes.getRoute(new GeoPoint(in.originLatitude(),in.originLongitude()),
                    new GeoPoint(w.building().latitude(),w.building().longitude()));
            presenter.present(new GetDirectionsOutputData(true,route.points(),route.distanceMeters(),route.timeSeconds(),"Route ready"));
        } catch (RuntimeException failure) {
            String message = failure.getMessage() == null ? "Directions are temporarily unavailable" : failure.getMessage();
            presenter.present(new GetDirectionsOutputData(false,List.of(),0,0,message));
        }
    }
}
