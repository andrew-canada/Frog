package interface_adapter.busyness;

import java.util.List;

import interface_adapter.common.AbstractViewModel;
import use_case.busyness.BusynessStatsOutputData;

public final class BusynessViewModel extends AbstractViewModel<BusynessViewModel.State> {
    public BusynessViewModel() {
        super(new State(List.of(), ""));
    }

    public record State(List<BusynessStatsOutputData.HourBucket> buckets, String note) {
        public State {
            buckets = List.copyOf(buckets);
        }
    }
}
