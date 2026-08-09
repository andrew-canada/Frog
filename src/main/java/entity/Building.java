package entity;

/**
 * A campus building. Coordinates are kept here rather than in UI code.
 */
public record Building(String code, String name, double latitude, double longitude) {
    public Building {
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Building code and name are required");
        }
    // Compatibility accessors retained for existing data-access code.
    }
    public String getBuildingCode() {
        return code;
    }

    public String getBuildingNameShort() {
        return name;
    }

    public String getBuildingNameLong() {
        return name;
    }

    public String getControlInfo() {
        return "";
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
