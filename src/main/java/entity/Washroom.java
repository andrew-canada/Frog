package entity;

import java.util.Objects;

public final class Washroom {
    private static final int VALUE_COUNT = 10;
    private static final int NAME_INDEX = 1;
    private static final int BUILDING_INDEX = 2;
    private static final int FLOOR_INDEX = 3;
    private static final int ACCESSIBLE_INDEX = 4;
    private static final int GENDER_INDEX = 5;
    private static final int TOILETS_INDEX = 6;
    private static final int SINKS_INDEX = 7;
    private static final int LOCATION_INDEX = 8;
    private static final int SUMMARY_INDEX = 9;
    private final String id;
    private final String name;
    private final Building building;
    private final String floor;
    private final boolean accessible;
    private final Gender gender;
    private final int numToilets;
    private final int numSinks;
    private final String locationDescription;
    private ReviewSummary reviewSummary;

    public Washroom(final Object... values) {
        if (values.length != VALUE_COUNT) {
            throw new IllegalArgumentException("Washroom requires ten values");
        }
        this.id = require((String) values[0], "id");
        this.name = require((String) values[NAME_INDEX], "name");
        this.building = Objects.requireNonNull((entity.Building) values[BUILDING_INDEX]);
        this.floor = require((String) values[FLOOR_INDEX], "floor");
        this.accessible = (Boolean) values[ACCESSIBLE_INDEX];
        this.gender = Objects.requireNonNull((Gender) values[GENDER_INDEX]);
        this.numToilets = positive(((Number) values[TOILETS_INDEX]).intValue(), "numToilets");
        this.numSinks = positive(((Number) values[SINKS_INDEX]).intValue(), "numSinks");
        this.locationDescription = require((String) values[LOCATION_INDEX], "locationDescription");
        this.reviewSummary = Objects.requireNonNull((ReviewSummary) values[SUMMARY_INDEX]);
    }

    private static String require(final String value, final String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static int positive(final int value, final String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " cannot be negative");
        }
        return value;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public String id() {
        return id;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public String name() {
        return name;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public entity.Building building() {
        return building;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public String floor() {
        return floor;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public boolean accessible() {
        return accessible;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public Gender gender() {
        return gender;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public int numToilets() {
        return numToilets;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public int numSinks() {
        return numSinks;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public String locationDescription() {
        return locationDescription;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public ReviewSummary reviewSummary() {
        return reviewSummary;
    }

    /**
     * Performs this operation.
     *
     * @param summary parameter value.
     */
    public void updateReviewSummary(final ReviewSummary summary) {
        this.reviewSummary = Objects.requireNonNull(summary);
    }

    public enum Gender {
        ALL_GENDER,
        WOMEN,
        MEN,
        WOMEN_AND_MEN,
        NO_INFO
    }
}
