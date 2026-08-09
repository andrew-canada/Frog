package use_case.port;

import entity.Washroom;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Read operations shared by washroom use cases.
 */
public interface WashroomRepository {
    Optional<Washroom> getById(String id);

    /**
     * Retrieves a displayed subset in one operation when the adapter supports it.
     * The default keeps smaller test and alternate adapters source-compatible.
     */
    default List<Washroom> getByIds(final Collection<String> ids) {
        return ids
            .stream()
            .map(this::getById)
            .flatMap(Optional::stream)
            .toList();
    }

    List<Washroom> getNearby(double latitude, double longitude, double radiusMeters);

    List<Washroom> getAll();
}
