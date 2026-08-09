package use_case.directions;

import java.util.List;

import entity.GeoPoint;

public record GetDirectionsOutputData(boolean success, List<GeoPoint> routePoints, int distanceMeters, int timeSeconds,
                                      String message) {
    public GetDirectionsOutputData {
        routePoints = List.copyOf(routePoints);
    }
}
