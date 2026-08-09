package use_case.filter;

import entity.Washroom;
import use_case.port.WashroomRepository;

import java.util.List;

/** Outbound port owned by the filter use case. */
public interface WashroomFilterRepository extends WashroomRepository {
    List<Washroom> findMatching(WashroomFilterCriteria criteria);
}
