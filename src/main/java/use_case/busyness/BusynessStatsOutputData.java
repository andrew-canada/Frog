package use_case.busyness;

import java.util.List;

public record BusynessStatsOutputData(List<HourBucket> buckets, String sourceNote) {
    public BusynessStatsOutputData {
        buckets = List.copyOf(buckets);
    }

    public record HourBucket(int hour, double busynessLevel, String dominantSource) {
    }
}
