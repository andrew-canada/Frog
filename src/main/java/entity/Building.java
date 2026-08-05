package entity;

/**
 * A campus building. Coordinates are kept here rather than in UI code.
 */
public record Building(String code, String name, double latitude,
                       double longitude) {
    public Building {
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Building code and name are required");
        }
    }
}
