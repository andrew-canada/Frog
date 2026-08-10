package database.washroom;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import entity.Building;
import entity.ReviewSummary;
import entity.Washroom;
import use_case.filter.WashroomFilterCriteria;
import use_case.filter.WashroomFilterRepository;

public final class InMemoryWashroomDataAccessObject implements WashroomFilterRepository {
    private static final int MAGIC_6_371_000 = 6_371_000;
    private static final int MAGIC_11 = 11;
    private static final double MAGIC_3_8 = 3.8;
    private static final double MAGIC_3_9 = 3.9;
    private static final int MAGIC_3 = 3;
    private static final int MAGIC_4 = 4;
    private static final int MAGIC_16 = 16;
    private static final double MAGIC_4_0 = 4.0;
    private static final double MAGIC_4_1 = 4.1;
    private static final int MAGIC_5 = 5;
    private static final int MAGIC_23 = 23;
    private static final double MAGIC_4_7 = 4.7;
    private static final double MAGIC_4_6 = 4.6;
    private final Map<String, Washroom> washrooms = new LinkedHashMap<>();

    public InMemoryWashroomDataAccessObject() {
        final Building bahen = new Building("BA", "Bahen Centre", 43.6597, -79.3974);
        final Building robarts = new Building("RB", "Robarts Library", 43.6644, -79.3996);
        final Building gerstein = new Building("GE", "Gerstein Library", 43.6626, -79.3934);
        add(new Washroom("bahen-2", "Bahen, 2nd floor", bahen, "2nd", true, Washroom.Gender.ALL_GENDER, MAGIC_3, 2,
            "Past the elevators", new ReviewSummary(MAGIC_4_6, MAGIC_4_7, MAGIC_23)));
        add(new Washroom("robarts-4", "Robarts, 4th floor", robarts, "4th", true, Washroom.Gender.ALL_GENDER, MAGIC_5,
            MAGIC_3,
            "North elevators", new ReviewSummary(MAGIC_4_1, MAGIC_4_0, MAGIC_16)));
        add(new Washroom("gerstein-main", "Gerstein, main floor", gerstein, "Main", false, Washroom.Gender.WOMEN,
            MAGIC_4, MAGIC_3,
            "East reading room", new ReviewSummary(MAGIC_3_9, MAGIC_3_8, MAGIC_11)));
    }

    private static double distance(final double lat1, final double lon1, final double lat2, final double lon2) {
        final double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        final double y = Math.toRadians(lat2 - lat1);
        return Math.sqrt(x * x + y * y) * MAGIC_6_371_000;
    }

    private void add(final Washroom washroom) {
        washrooms.put(washroom.id(), washroom);
        final List<Washroom> ordered = washrooms
            .values()
            .stream()
            .sorted(Comparator
                .comparing((Washroom value) -> {
                    return value
                        .building()
                        .name();
                }, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Washroom::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Washroom::id))
            .toList();
        washrooms.clear();
        ordered.forEach(value -> {
            washrooms.put(value.id(), value);
        });
    }

    @Override
    public Optional<Washroom> getById(final String id) {
        return Optional.ofNullable(washrooms.get(id));
    }

    @Override
    public List<Washroom> getByIds(final Collection<String> ids) {
        return ids
            .stream()
            .map(washrooms::get)
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public List<Washroom> getAll() {
        return List.copyOf(washrooms.values());
    }

    @Override
    public List<Washroom> getNearby(final double lat, final double lng, final double radiusMeters) {
        return washrooms
            .values()
            .stream()
            .filter(washroomValue -> {
                return distance(lat, lng, washroomValue
                    .building()
                    .latitude(), washroomValue
                    .building()
                    .longitude()) <= radiusMeters;
            })
            .toList();
    }

    @Override
    public List<Washroom> findMatching(final WashroomFilterCriteria criteria) {
        return washrooms
            .values()
            .stream()
            .filter(washroomValue -> {
                return !criteria.accessibleOnly() || washroomValue.accessible();
            })
            .filter(washroom -> {
                return criteria.gender() == null || washroom.gender() == criteria.gender();
            })
            .filter(washroom -> {
                return criteria.buildingCode() == null || criteria
                    .buildingCode()
                    .isBlank() || washroom
                    .building()
                    .code()
                    .equals(criteria.buildingCode());
            })
            .filter(washroom -> {
                return criteria
                    .permittedNames()
                    .isEmpty() || criteria
                    .permittedNames()
                    .contains(washroom.name());
            })
            .toList();
    }
}
