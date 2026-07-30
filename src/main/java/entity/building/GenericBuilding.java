package entity.building;

import entity.washroom.Washroom;
import java.util.List;

public class GenericBuilding implements Building {
    private String buildingCode;
    private String buildingNameShort;
    private String buildingNameLong;
    private double latitude;
    private double longitude;
    private String controlInfo;

    public GenericBuilding(
            String buildingCode,
            String buildingNameShort,
            String buildingNameLong,
            double latitude,
            double longitude,
            String controlInfo
    ) {
        this.buildingCode = buildingCode;
        this.buildingNameShort = buildingNameShort;
        this.buildingNameLong = buildingNameLong;
        this.latitude = latitude;
        this.longitude = longitude;
        this.controlInfo = controlInfo;
    }

    @Override
    public String getBuildingCode() {
        return buildingCode;
    }

    @Override
    public String getBuildingNameShort() {
        return buildingNameShort;
    }

    @Override
    public String getBuildingNameLong() {
        return buildingNameLong;
    }

    @Override
    public String getControlInfo() {
        return controlInfo;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
