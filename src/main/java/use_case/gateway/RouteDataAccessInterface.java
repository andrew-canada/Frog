package use_case.gateway;

import entity.GeoPoint;
import entity.Route;

public interface RouteDataAccessInterface {
    Route getRoute(GeoPoint origin, GeoPoint destination);
}
