package use_case.filter;

import entity.Washroom;
import java.util.List;
import use_case.port.WashroomRepository;

/**
 * Outbound port owned by the filter use case.
 */
public interface WashroomFilterRepository extends WashroomRepository {
    List<Washroom> findMatching(WashroomFilterCriteria criteria);
}
