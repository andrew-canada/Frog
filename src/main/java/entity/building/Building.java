package entity.building;

import entity.washroom.Washroom;

import java.util.List;

public interface Building {

    public String getBuildingCode();

    public String getBuildingNameShort();

    public String getBuildingNameLong();

    public String getControlInfo();

    public List<Washroom> getWashroomList() ;
}
