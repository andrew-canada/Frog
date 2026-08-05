package interface_adapter.directions;

import entity.GeoPoint;
import interface_adapter.common.ViewModel;

import java.util.List;

public final class MapViewModel extends ViewModel<MapViewModel.State> {
    public MapViewModel() {
        super(new State(false, List.of(), "", "", ""));
    }

    public record State(boolean success, List<GeoPoint> points, String distance, String duration, String message) {
        public State {
            points = List.copyOf(points);
        }
    }
}
