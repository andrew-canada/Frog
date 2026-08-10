package use_case.filter;

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
public record WashroomFilterCriteria(boolean accessibleOnly, Washroom.Gender gender, String buildingCode,
                                     Set<String> permittedNames) {
    public WashroomFilterCriteria {
        permittedNames = Set.copyOf(permittedNames);
    }
}
