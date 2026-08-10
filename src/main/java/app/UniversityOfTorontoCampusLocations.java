package app;

import java.util.List;

import entity.Building;

/**
 * Stable reference locations shown on the St. George campus map.
 */
final class UniversityOfTorontoCampusLocations {
    private static final double HART_HOUSE_LONGITUDE = -79.394444;
    private static final double HART_HOUSE_LATITUDE = 43.663611;
    private static final double TRINITY_COLLEGE_LONGITUDE = -79.395833;
    private static final double TRINITY_COLLEGE_LATITUDE = 43.665556;
    private static final double MYHAL_CENTRE_LONGITUDE = -79.396485;
    private static final double MYHAL_CENTRE_LATITUDE = 43.660837;
    private static final double BAHEN_CENTRE_LONGITUDE = -79.397200;
    private static final double BAHEN_CENTRE_LATITUDE = 43.659600;

    private UniversityOfTorontoCampusLocations() {
    }

    /**
     * Returns the stable reference locations shown on the campus map.
     *
     * @return campus reference locations
     */
    public static List<Building> coreLocations() {
        return List.of(new Building("BA", "Bahen Centre for Information Technology", BAHEN_CENTRE_LATITUDE,
            BAHEN_CENTRE_LONGITUDE),
            new Building("MY", "Myhal Centre for Engineering Innovation & Entrepreneurship", MYHAL_CENTRE_LATITUDE,
                MYHAL_CENTRE_LONGITUDE),
            new Building("TC", "Trinity College", TRINITY_COLLEGE_LATITUDE, TRINITY_COLLEGE_LONGITUDE),
            new Building("HH", "Hart House", HART_HOUSE_LATITUDE, HART_HOUSE_LONGITUDE));
    }
}
