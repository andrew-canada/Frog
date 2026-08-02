package entity;

/**
 * A campus building. Coordinates are kept here rather than in UI code.
 */
public record Building(String code, String name, double latitude,
                       double longitude) implements entity.building.Building {
    public Building {
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Building code and name are required");
        }
    }

    @Override
    public String getBuildingCode() {
        return code;
    }

    @Override
    public String getBuildingNameShort() {
        return name;
    }

    @Override
    public String getBuildingNameLong() {
        return name;
    }

    @Override
    public String getControlInfo() {
        return "";
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }
}
