package use_case.filter;

import java.util.List;

import entity.Washroom;
import use_case.port.WashroomRepository;

/**
 * Outbound port owned by the filter use case.
 */
public interface WashroomFilterRepository extends WashroomRepository {
    /**
     * Performs this operation.
     *
     * @param criteria parameter value.
     *
     * @return the operation result.
     */
    List<Washroom> findMatching(WashroomFilterCriteria criteria);
}
