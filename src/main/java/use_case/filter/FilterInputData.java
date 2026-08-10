package use_case.filter;

/**
 * Input values for filtering washrooms.
 */
public final class FilterInputData {
    private static final int VALUE_COUNT = 9;
    private static final int MIN_CLEANLINESS_INDEX = 1;
    private static final int ACCESSIBLE_INDEX = 2;
    private static final int GENDER_INDEX = 3;
    private static final int WASHROOM_ID_INDEX = 4;
    private static final int OWN_REVIEWS_INDEX = 5;
    private static final int PERSONAL_PLAN_INDEX = 6;
    private static final int LATITUDE_INDEX = 7;
    private static final int LONGITUDE_INDEX = 8;
    private final float maxBusyness;
    private final float minCleanliness;
    private final boolean accessible;
    private final String gender;
    private final String washroomID;
    private final boolean ownReviews;
    private final boolean personalPlan;
    private final double latitude;
    private final double longitude;

    public FilterInputData(final Object... values) {
        if (values.length != VALUE_COUNT) {
            throw new IllegalArgumentException("FilterInputData requires nine values");
        }
        this.maxBusyness = ((Number) values[0]).floatValue();
        this.minCleanliness = ((Number) values[MIN_CLEANLINESS_INDEX]).floatValue();
        this.accessible = (Boolean) values[ACCESSIBLE_INDEX];
        this.gender = (String) values[GENDER_INDEX];
        this.washroomID = (String) values[WASHROOM_ID_INDEX];
        this.ownReviews = (Boolean) values[OWN_REVIEWS_INDEX];
        this.personalPlan = (Boolean) values[PERSONAL_PLAN_INDEX];
        this.latitude = ((Number) values[LATITUDE_INDEX]).doubleValue();
        this.longitude = ((Number) values[LONGITUDE_INDEX]).doubleValue();
    }

    float maxBusyness() {
        return maxBusyness;
    }

    float minCleanliness() {
        return minCleanliness;
    }

    boolean accessible() {
        return accessible;
    }

    String gender() {
        return gender;
    }

    String washroomID() {
        return washroomID;
    }

    boolean ownReviews() {
        return ownReviews;
    }

    boolean personalPlan() {
        return personalPlan;
    }

    double latitude() {
        return latitude;
    }

    double longitude() {
        return longitude;
    }
}
