package interface_adapter.filter;

import java.util.ArrayList;
import java.util.List;

import entity.Washroom;
import interface_adapter.common.ViewModel;

public final class FilterViewModel extends ViewModel<interface_adapter.filter.FilterViewModel.State> {
    public FilterViewModel() {
        super(new interface_adapter.filter.FilterViewModel.State(false, new ArrayList<>(), ""));
    }

    public record State(boolean success, List<Washroom> washrooms, String message) {
        public State {
        }
    }
}
