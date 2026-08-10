package use_case.port;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import entity.Washroom;

/**
 * Read operations shared by washroom use cases.
 */
public interface WashroomRepository {
    /**
     * Performs this operation.
     *
     * @param id parameter value.
     *
     * @return the operation result.
     */
    Optional<Washroom> getById(String id);

    /**
     * Retrieves a displayed subset in one operation when the adapter supports it.
     * The default keeps smaller test and alternate adapters source-compatible.
     * @param ids parameter value.
     * @return the operation result.
     */
    default List<Washroom> getByIds(final Collection<String> ids) {
        return ids
            .stream()
            .map(this::getById)
            .flatMap(Optional::stream)
            .toList();
    }

    /**
     * Performs this operation.
     *
     * @param latitude parameter value.
     *
     * @param longitude parameter value.
     *
     * @param radiusMeters parameter value.
     *
     * @return the operation result.
     */
    List<Washroom> getNearby(double latitude, double longitude, double radiusMeters);

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    List<Washroom> getAll();
}
