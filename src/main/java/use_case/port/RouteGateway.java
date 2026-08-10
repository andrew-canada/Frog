package use_case.port;

import entity.GeoPoint;
import entity.Route;

public interface RouteGateway {
    /**
     * Performs this operation.
     *
     * @param origin parameter value.
     *
     * @param destination parameter value.
     *
     * @return the operation result.
     */
    Route getRoute(GeoPoint origin, GeoPoint destination);
}
