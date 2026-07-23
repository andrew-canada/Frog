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
    private List<Washroom> washroomList;

    public GenericBuilding(
            String buildingCode,
            String buildingNameShort,
            String buildingNameLong,
            double latitude,
            double longitude,
            String controlInfo,
            List<Washroom> washroomList
    ) {
        this.buildingCode = buildingCode;
        this.buildingNameShort = buildingNameShort;
        this.buildingNameLong = buildingNameLong;
        this.latitude = latitude;
        this.longitude = longitude;
        this.controlInfo = controlInfo;
        this.washroomList = washroomList;
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

    @Override
    public List<Washroom> getWashroomList() {
        return washroomList;
    }
}
