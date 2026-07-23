package entity.building;

import entity.washroom.Washroom;

import java.util.List;

public class GenericBuildingFactory {
    GenericBuilding create(
            String buildingCode,
            String buildingNameShort,
            String buildingNameLong,
            double latitude,
            double longitude,
            String controlInfo,
            List<Washroom> washroomList
    ) {
        return new GenericBuilding(
                buildingCode,
                buildingNameShort,
                buildingNameLong,
                latitude,
                longitude,
                controlInfo,
                washroomList
                );
    }
}
