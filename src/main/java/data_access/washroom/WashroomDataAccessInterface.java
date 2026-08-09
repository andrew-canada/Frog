package data_access.washroom;

import data_access.AbstractCondition;
import entity.Washroom;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WashroomDataAccessInterface {
    Optional<Washroom> getById(String id);

    List<Washroom> getNearby(double latitude, double longitude, double radiusMeters);

    List<Washroom> getAll();

    default List<Washroom> getMatching(Iterable<AbstractCondition<?>> conditions) {
        throw new UnsupportedOperationException("Matching washrooms is not supported by this data source");
    }

    default Map<String, Washroom> getMatchingIDMap(Iterable<AbstractCondition<?>> conditions) {
        throw new UnsupportedOperationException("Matching washrooms by ID is not supported by this data source");
    }

    default String write(Washroom washroom, String buildingID) {
        throw new UnsupportedOperationException("Writing washrooms is not supported by this data source");
    }

}
