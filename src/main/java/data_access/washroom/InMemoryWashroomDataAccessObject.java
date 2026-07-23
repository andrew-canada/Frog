package data_access.washroom;

import entity.Building;
import entity.ReviewSummary;
import entity.Washroom;
import use_case.gateway.WashroomDataAccessInterface;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryWashroomDataAccessObject implements WashroomDataAccessInterface {
    private final Map<String, Washroom> washrooms = new LinkedHashMap<>();

    public InMemoryWashroomDataAccessObject() {
        Building bahen = new Building("BA", "Bahen Centre", 43.6597, -79.3974);
        Building robarts = new Building("RB", "Robarts Library", 43.6644, -79.3996);
        Building gerstein = new Building("GE", "Gerstein Library", 43.6626, -79.3934);
        add(new Washroom("bahen-2", "Bahen, 2nd floor", bahen, "2nd", true,
                Washroom.Gender.ALL_GENDER, 3, 2, "Past the elevators",
                new ReviewSummary(4.6, 4.7, 23)));
        add(new Washroom("robarts-4", "Robarts, 4th floor", robarts, "4th", true,
                Washroom.Gender.ALL_GENDER, 5, 3, "North elevators",
                new ReviewSummary(4.1, 4.0, 16)));
        add(new Washroom("gerstein-main", "Gerstein, main floor", gerstein, "Main", false,
                Washroom.Gender.WOMEN, 4, 3, "East reading room",
                new ReviewSummary(3.9, 3.8, 11)));
    }

    public void add(Washroom washroom) { washrooms.put(washroom.id(), washroom); }
    @Override public Optional<Washroom> getById(String id) { return Optional.ofNullable(washrooms.get(id)); }
    @Override public List<Washroom> getAll() { return List.copyOf(washrooms.values()); }
    @Override public List<Washroom> getNearby(double lat, double lng, double radiusMeters) {
        return washrooms.values().stream().filter(w -> distance(lat, lng,
                w.building().latitude(), w.building().longitude()) <= radiusMeters).toList();
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double y = Math.toRadians(lat2 - lat1);
        return Math.sqrt(x * x + y * y) * 6_371_000;
    }
}
