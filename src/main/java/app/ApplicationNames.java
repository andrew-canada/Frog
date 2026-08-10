package app;

import java.util.Set;

/** Washroom names loaded from the bundled JSON baseline. */
final class ApplicationNames {
    private static final Set<String> VALUES = CampusStartup.loadWashroomNames();

    private ApplicationNames() {
    }

    static Set<String> values() {
        return VALUES;
    }
}
