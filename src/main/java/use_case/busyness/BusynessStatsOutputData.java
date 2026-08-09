package use_case.busyness;

import java.util.List;

public record BusynessStatsOutputData(List<HourBucket> buckets, String sourceNote) {
    public BusynessStatsOutputData {
        buckets = List.copyOf(buckets);
    }

    public record HourBucket(int hour, double busynessLevel, double cleanlinessLevel, String dominantSource) {
        public HourBucket(final int hour, final double busynessLevel, final String dominantSource) {
            this(hour, busynessLevel, 0, dominantSource);
        }
    }
}
