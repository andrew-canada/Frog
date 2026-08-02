package interface_adapter.busyness;

import interface_adapter.common.ViewModel;
import use_case.busyness.BusynessStatsOutputData;

import java.util.List;

public final class BusynessViewModel extends ViewModel<BusynessViewModel.State> {
    public BusynessViewModel() {
        super(new State(List.of(), ""));
    }

    public record State(List<BusynessStatsOutputData.HourBucket> buckets, String note) {
        public State {
            buckets = List.copyOf(buckets);
        }
    }
}
