package data_access.route;

import entity.GeoPoint;
import entity.Route;
import use_case.gateway.RouteDataAccessInterface;
import java.util.List;

/** Demo-safe GraphHopper adapter. Replace this deterministic stub with an HTTP client later. */
public final class GraphhopperRouteDataAccessObject implements RouteDataAccessInterface {
    @Override public Route getRoute(GeoPoint from, GeoPoint to) {
        GeoPoint middle = new GeoPoint((from.latitude() + to.latitude()) / 2 + .0005,
                (from.longitude() + to.longitude()) / 2);
        int meters = (int) Math.round(distance(from, to) * 1.12);
        return new Route(List.of(from, middle, to), meters, Math.max(60, (int) (meters / 1.35)));
    }
    private static double distance(GeoPoint a, GeoPoint b) {
        double x = Math.toRadians(b.longitude() - a.longitude()) * Math.cos(Math.toRadians((a.latitude() + b.latitude()) / 2));
        double y = Math.toRadians(b.latitude() - a.latitude());
        return Math.sqrt(x * x + y * y) * 6_371_000;
    }
}
