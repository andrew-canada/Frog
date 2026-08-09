package use_case.port;

import entity.GeoPoint;
import entity.Route;

public interface RouteGateway {
    Route getRoute(GeoPoint origin, GeoPoint destination);
}
