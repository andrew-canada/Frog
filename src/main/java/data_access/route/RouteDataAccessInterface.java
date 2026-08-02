package data_access.route;

import entity.GeoPoint;
import entity.Route;

public interface RouteDataAccessInterface {
    Route getRoute(GeoPoint origin, GeoPoint destination);
}
