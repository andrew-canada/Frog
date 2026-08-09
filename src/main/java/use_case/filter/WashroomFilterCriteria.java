package use_case.filter;

import entity.Washroom;

import java.util.Set;

/**
 * Application query vocabulary for washroom filtering. Infrastructure adapters
 * translate this value object to their own query language.
 */
public record WashroomFilterCriteria(boolean accessibleOnly, Washroom.Gender gender,
                                    String buildingCode, Set<String> permittedNames) {
    public WashroomFilterCriteria {
        permittedNames = Set.copyOf(permittedNames);
    }
}
