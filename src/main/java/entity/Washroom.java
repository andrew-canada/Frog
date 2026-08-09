package entity;

import java.util.Objects;

public final class Washroom {
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

    public Washroom(final String id, final String name, final entity.Building building, final String floor, final boolean accessible,
                    final Gender gender, final int numToilets, final int numSinks, final String locationDescription,
                    final ReviewSummary reviewSummary) {
        this.id = require(id, "id");
        this.name = require(name, "name");
        this.building = Objects.requireNonNull(building);
        this.floor = require(floor, "floor");
        this.accessible = accessible;
        this.gender = Objects.requireNonNull(gender);
        this.numToilets = positive(numToilets, "numToilets");
        this.numSinks = positive(numSinks, "numSinks");
        this.locationDescription = require(locationDescription, "locationDescription");
        this.reviewSummary = Objects.requireNonNull(reviewSummary);
    }

    private static String require(final String value, final String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value;
    }

    private static int positive(final int value, final String field) {
        if (value < 0) throw new IllegalArgumentException(field + " cannot be negative");
        return value;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public entity.Building building() {
        return building;
    }

    public String floor() {
        return floor;
    }

    public boolean accessible() {
        return accessible;
    }

    public Gender gender() {
        return gender;
    }

    public int numToilets() {
        return numToilets;
    }

    public int numSinks() {
        return numSinks;
    }

    public String locationDescription() {
        return locationDescription;
    }

    public ReviewSummary reviewSummary() {
        return reviewSummary;
    }

    public void updateReviewSummary(final ReviewSummary summary) {
        this.reviewSummary = Objects.requireNonNull(summary);
    }

    public enum Gender { ALL_GENDER, WOMEN, MEN }
}
