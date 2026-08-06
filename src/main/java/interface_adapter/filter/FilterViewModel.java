package interface_adapter.filter;

import entity.Washroom;
import interface_adapter.common.ViewModel;

import java.util.ArrayList;
import java.util.List;

public final class FilterViewModel extends ViewModel<interface_adapter.filter.FilterViewModel.State> {
    public FilterViewModel() {
        super(new interface_adapter.filter.FilterViewModel.State(false, new ArrayList<>(), ""));
    }

    public record State(boolean success, List<Washroom> washrooms, String message) {
        public State {
        }
    }
}
