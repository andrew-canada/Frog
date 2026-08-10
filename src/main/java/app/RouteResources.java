package app;

import database.route.GraphhopperGeocodingDataAccessObject;
import database.route.GraphhopperRouteDataAccessObject;

/** GraphHopper resources shared by the application. */
final class RouteResources {
    private final GraphhopperRouteDataAccessObject routes;
    private final GraphhopperGeocodingDataAccessObject geocoding;

    RouteResources(final GraphhopperRouteDataAccessObject routes,
                   final GraphhopperGeocodingDataAccessObject geocoding) {
        this.routes = routes;
        this.geocoding = geocoding;
    }

    GraphhopperRouteDataAccessObject routes() {
        return routes;
    }

    GraphhopperGeocodingDataAccessObject geocoding() {
        return geocoding;
    }
}
