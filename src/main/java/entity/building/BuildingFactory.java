package entity.building;

import entity.washroom.Washroom;

import java.util.List;

public interface BuildingFactory {
    Building create(
            String buildingCode,
            String buildingNameShort,
            String buildingNameLong,
            double latitude,
            double longitude,
            String controlInfo,
            List<Washroom> washroomList
    );
}
