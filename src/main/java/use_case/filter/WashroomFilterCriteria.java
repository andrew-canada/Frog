package use_case.filter;

import java.util.List;
import java.util.Set;

import entity.Washroom;

/**
 * Application query vocabulary for washroom filtering. Infrastructure adapters
 * translate this value object to their own query language.
 * @param accessibleOnly parameter value.
 * @param buildingCode parameter value.
 * @param gender parameter value.
 * @param permittedNames parameter value.
 */
public record WashroomFilterCriteria(boolean accessibleOnly, List<Washroom.Gender> gender, String buildingCode,
    Set<String> permittedNames) {
    public WashroomFilterCriteria {
        gender = immutableGender(gender);
        permittedNames = Set.copyOf(permittedNames);
    }

    private static List<Washroom.Gender> immutableGender(final List<Washroom.Gender> genders) {
        List<Washroom.Gender> result = null;
        if (genders != null) {
            result = List.copyOf(genders);
        }
        return result;
    }
}
