package app;

import java.util.List;

import entity.Building;

/**
 * Stable reference locations shown on the St. George campus map.
 */
public final class UofTCampusLocations {
    private UofTCampusLocations() {
    }

    public static List<Building> coreLocations() {
        return List.of(new Building("BA", "Bahen Centre for Information Technology", 43.659600, -79.397200),
            new Building("MY", "Myhal Centre for Engineering Innovation & Entrepreneurship", 43.660837, -79.396485),
            new Building("TC", "Trinity College", 43.665556, -79.395833),
            new Building("HH", "Hart House", 43.663611, -79.394444));
    }
}
