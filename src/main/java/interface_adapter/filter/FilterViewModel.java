package interface_adapter.filter;

import interface_adapter.common.ViewModel;

public final class FilterViewModel extends ViewModel<interface_adapter.filter.FilterViewModel.State> {
        public FilterViewModel() {
            super(new interface_adapter.filter.FilterViewModel.State(false, ""));
        }

        public record State(boolean success, String message) {
            public State {}
        }
}
