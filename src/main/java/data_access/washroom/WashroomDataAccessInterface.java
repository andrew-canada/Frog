package data_access.washroom;

import entity.Washroom;

import java.util.List;
import java.util.Optional;

public interface WashroomDataAccessInterface {
    Optional<Washroom> getById(String id);

    List<Washroom> getNearby(double latitude, double longitude, double radiusMeters);

    List<Washroom> getAll();

}
