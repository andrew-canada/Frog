package interface_adapter.sort_washrooms;

import java.util.List;

import entity.Washroom;
import interface_adapter.common.AbstractViewModel;

public final class SortWashroomViewModel extends AbstractViewModel<SortWashroomViewModel.State> {
    public SortWashroomViewModel() {
        super(new State(false, List.of()));
    }

    public record State(boolean success, List<Washroom> washrooms) {
        public State {
            washrooms = List.copyOf(washrooms);
        }
    }
}
