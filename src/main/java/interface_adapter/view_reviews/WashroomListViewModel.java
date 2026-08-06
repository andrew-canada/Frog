package interface_adapter.view_reviews;

import interface_adapter.common.ViewModel;

import java.util.List;

public final class WashroomListViewModel extends ViewModel<WashroomListViewModel.State> {
    public WashroomListViewModel() {
        super(new State(List.of(), null, "Alphabetical", false));
    }

    public record Item(String id, String name, String description, double rating, int distanceMeters, boolean accessible) {
        public Item(String id, String name, double rating, int distanceMeters, boolean accessible) {
            this(id, name, "", rating, distanceMeters, accessible);
        }
    }

    public record State(List<Item> items, String selectedId, String sortLabel, boolean routeVisible) {
        public State {
            items = List.copyOf(items);
        }
    }
}
