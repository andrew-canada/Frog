package interface_adapter.view_reviews;

import java.util.Comparator;
import java.util.List;

import interface_adapter.common.AbstractViewModel;

public final class WashroomListViewModel extends AbstractViewModel<WashroomListViewModel.State> {
    public WashroomListViewModel() {
        super(new State(List.of(), null, "Sort by: Nearest", false));
    }

    public record Item(String id, String name, String description, double rating, int distanceMeters,
                       boolean accessible) {
        public static final Comparator<Item> BY_DISTANCE = Comparator.comparing(Item::distanceMeters);
        public static final Comparator<Item> BY_RATING = Comparator
            .comparing(Item::rating)
            .reversed();
        public static final Comparator<Item> BY_ALPHABETICAL = Comparator.comparing(Item::name);
    }

    public record State(List<Item> items, String selectedId, String sortLabel, boolean routeVisible) {
        public State {
            items = List.copyOf(items);
        }
    }
}
