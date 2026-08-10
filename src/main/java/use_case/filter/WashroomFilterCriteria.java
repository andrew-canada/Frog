package use_case.filter;

import java.util.List;
import java.util.Set;

import entity.Washroom;

/**
 * Application query vocabulary for washroom filtering. Infrastructure adapters
 * translate this value object to their own query language.
 */
public record WashroomFilterCriteria(boolean accessibleOnly, List<Washroom.Gender> gender, String buildingCode,
                                     Set<String> permittedNames) {
    public WashroomFilterCriteria {
        permittedNames = Set.copyOf(permittedNames);
    }
}
