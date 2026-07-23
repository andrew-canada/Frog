package use_case.directions;

import entity.GeoPoint;
import java.util.List;

public record GetDirectionsOutputData(boolean success, List<GeoPoint> routePoints,
        int distanceMeters, int timeSeconds, String message) {
    public GetDirectionsOutputData { routePoints=List.copyOf(routePoints); }
}
