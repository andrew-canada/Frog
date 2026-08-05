package data_access.washroom;

import data_access.AbstractCondition;
import data_access.Condition;
import entity.Washroom;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WashroomDataAccessInterface {
    Optional<Washroom> getById(String id);

    List<Washroom> getNearby(double latitude, double longitude, double radiusMeters);

    List<Washroom> getAll();

    List<Washroom> getMatching(Iterable<AbstractCondition<?>> conditions);

    Map<String, Washroom> getMatchingIDMap(Iterable<AbstractCondition<?>> conditions);

}
