package interface_adapter.busyness;

import interface_adapter.common.ViewModel;
import java.util.List;
import use_case.busyness.BusynessStatsOutputData;

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
