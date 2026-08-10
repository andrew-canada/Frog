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
    private static final int EARTH_RADIUS_METERS = 6_371_000;
    private static final int SAMPLE_REVIEW_COUNT_ELEVEN = 11;
    private static final double SAMPLE_CLEANLINESS_SCORE_THREE_POINT_EIGHT = 3.8;
    private static final double SAMPLE_RATING_SCORE_THREE_POINT_NINE = 3.9;
    private static final int SAMPLE_COUNT_THREE = 3;
    private static final int SAMPLE_COUNT_FOUR = 4;
    private static final int SAMPLE_REVIEW_COUNT_SIXTEEN = 16;
    private static final double SAMPLE_CLEANLINESS_SCORE_FOUR = 4.0;
    private static final double SAMPLE_RATING_SCORE_FOUR_POINT_ONE = 4.1;
    private static final int SAMPLE_COUNT_FIVE = 5;
    private static final int SAMPLE_REVIEW_COUNT_TWENTY_THREE = 23;
    private static final double SAMPLE_CLEANLINESS_SCORE_FOUR_POINT_SEVEN = 4.7;
    private static final double SAMPLE_RATING_SCORE_FOUR_POINT_SIX = 4.6;
    private final Map<String, Washroom> washrooms = new LinkedHashMap<>();

    public InMemoryWashroomDataAccessObject() {
        final Building bahen = new Building("BA", "Bahen Centre", 43.6597, -79.3974);
        final Building robarts = new Building("RB", "Robarts Library", 43.6644, -79.3996);
        final Building gerstein = new Building("GE", "Gerstein Library", 43.6626, -79.3934);
        add(new Washroom("bahen-2", "Bahen, 2nd floor", bahen, "2nd", true, Washroom.Gender.ALL_GENDER,
            SAMPLE_COUNT_THREE, 2, "Past the elevators",
            new ReviewSummary(SAMPLE_RATING_SCORE_FOUR_POINT_SIX, SAMPLE_CLEANLINESS_SCORE_FOUR_POINT_SEVEN,
                SAMPLE_REVIEW_COUNT_TWENTY_THREE)));
        add(new Washroom("robarts-4", "Robarts, 4th floor", robarts, "4th", true, Washroom.Gender.ALL_GENDER,
            SAMPLE_COUNT_FIVE, SAMPLE_COUNT_THREE, "North elevators",
            new ReviewSummary(SAMPLE_RATING_SCORE_FOUR_POINT_ONE, SAMPLE_CLEANLINESS_SCORE_FOUR,
                SAMPLE_REVIEW_COUNT_SIXTEEN)));
        add(new Washroom("gerstein-main", "Gerstein, main floor", gerstein, "Main", false, Washroom.Gender.WOMEN,
            SAMPLE_COUNT_FOUR, SAMPLE_COUNT_THREE, "East reading room",
            new ReviewSummary(SAMPLE_RATING_SCORE_THREE_POINT_NINE, SAMPLE_CLEANLINESS_SCORE_THREE_POINT_EIGHT,
                SAMPLE_REVIEW_COUNT_ELEVEN)));
    }

    private static double distance(final double lat1, final double lon1, final double lat2, final double lon2) {
        final double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        final double y = Math.toRadians(lat2 - lat1);
        return Math.sqrt(x * x + y * y) * EARTH_RADIUS_METERS;
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
