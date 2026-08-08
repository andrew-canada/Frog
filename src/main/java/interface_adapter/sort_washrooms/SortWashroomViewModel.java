package interface_adapter.sort_washrooms;

import entity.Washroom;
import interface_adapter.common.ViewModel;

import java.util.ArrayList;
import java.util.List;

public final class SortWashroomViewModel extends ViewModel<interface_adapter.sort_washrooms.SortWashroomViewModel.State> {
    public SortWashroomViewModel() {
        super(new SortWashroomViewModel.State(false, new ArrayList<>()));
    }

    public record State(boolean success, List<Washroom> washrooms) {
        public State{

        }
    }
}
