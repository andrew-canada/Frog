package interface_adapter.sort_washrooms;

import entity.Washroom;
import interface_adapter.common.ViewModel;

import java.util.List;

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
