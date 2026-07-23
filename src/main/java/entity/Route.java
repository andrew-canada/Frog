package entity;

import java.util.List;

public record Route(List<GeoPoint> points, int distanceMeters, int timeSeconds) {
    public Route { points = List.copyOf(points); }
}
