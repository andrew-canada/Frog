package interface_adapter.directions;
import use_case.directions.GetDirectionsOutputBoundary;
import use_case.directions.GetDirectionsOutputData;
public final class DirectionsPresenter implements GetDirectionsOutputBoundary{
    private final MapViewModel model;public DirectionsPresenter(MapViewModel model){this.model=model;}
    @Override public void present(GetDirectionsOutputData d){model.setState(new MapViewModel.State(d.success(),d.routePoints(),d.distanceMeters()+" m",Math.max(1,d.timeSeconds()/60)+" min"));}
}
