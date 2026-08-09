package interface_adapter.directions;

import java.util.List;

import entity.GeoPoint;
import interface_adapter.common.ViewModel;

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
