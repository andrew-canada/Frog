import use_case.port.RouteGateway;
import use_case.port.WashroomRepository;
import entity.Route;
import entity.Washroom;
import use_case.directions.GetDirectionsInputData;
import use_case.directions.GetDirectionsInteractor;
import use_case.directions.GetDirectionsOutputData;

import java.util.List;
import java.util.Optional;

final class GetDirectionsInteractorTest {
    static void run() {
        Washroom w = TestSupport.washroom();
        WashroomRepository washrooms = new WashroomRepository() {
            public Optional<Washroom> getById(String id) {
                return Optional.of(w);
            }

            public List<Washroom> getNearby(double a, double b, double r) {
                return List.of(w);
            }

            public List<Washroom> getAll() {
                return List.of(w);
            }
        };
        RouteGateway routes = (a, b) -> new Route(List.of(a, b), 480, 360);
        final GetDirectionsOutputData[] out = new GetDirectionsOutputData[1];
        new GetDirectionsInteractor(washrooms, routes, d -> out[0] = d).execute(new GetDirectionsInputData(43.65, -79.38, "w1"));
        TestSupport.check(out[0].success() && out[0].distanceMeters() == 480 && out[0].routePoints().size() == 2, "route output");
    }
}
