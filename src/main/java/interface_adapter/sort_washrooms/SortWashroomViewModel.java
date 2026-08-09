package interface_adapter.sort_washrooms;

import java.util.List;

import entity.Washroom;
import interface_adapter.common.ViewModel;

public final class SortWashroomViewModel extends ViewModel<SortWashroomViewModel.State> {
    public SortWashroomViewModel() {
        super(new State(false, List.of()));
    }

    public record State(boolean success, List<Washroom> washrooms) {
        public State {
            washrooms = List.copyOf(washrooms);
        }
    }
}
