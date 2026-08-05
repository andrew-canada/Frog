import data_access.route.RouteDataAccessInterface;
import data_access.washroom.WashroomDataAccessInterface;
import entity.*;
import use_case.directions.*;

import java.util.*;

final class GetDirectionsInteractorTest {
    static void run() {
        Washroom w = TestSupport.washroom();
        WashroomDataAccessInterface washrooms = new WashroomDataAccessInterface() {
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
        RouteDataAccessInterface routes = (a, b) -> new Route(List.of(a, b), 480, 360);
        final GetDirectionsOutputData[] out = new GetDirectionsOutputData[1];
        new GetDirectionsInteractor(washrooms, routes, d -> out[0] = d).execute(new GetDirectionsInputData(43.65, -79.38, "w1"));
        TestSupport.check(out[0].success() && out[0].distanceMeters() == 480 && out[0].routePoints().size() == 2, "route output");
    }
}
