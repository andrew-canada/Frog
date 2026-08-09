import entity.Route;
import entity.Washroom;
import java.util.List;
import java.util.Optional;
import use_case.directions.GetDirectionsInputData;
import use_case.directions.GetDirectionsInteractor;
import use_case.directions.GetDirectionsOutputData;
import use_case.port.RouteGateway;
import use_case.port.WashroomRepository;

final class GetDirectionsInteractorTest {
    static void run() {
        final Washroom w = TestSupport.washroom();
        final WashroomRepository washrooms = new WashroomRepository() {
            @Override
            public Optional<Washroom> getById(final String id) {
                return Optional.of(w);
            }

            @Override
            public List<Washroom> getNearby(final double a, final double b, final double r) {
                return List.of(w);
            }

            @Override
            public List<Washroom> getAll() {
                return List.of(w);
            }
        };
        final RouteGateway routes = (a, b) -> {
            return new Route(List.of(a, b), 480, 360);
        };
        final GetDirectionsOutputData[] out = new GetDirectionsOutputData[1];
        new GetDirectionsInteractor(washrooms, routes, d -> {
            out[0] = d;
        }).execute(
            new GetDirectionsInputData(43.65, -79.38, "w1"));
        TestSupport.check(out[0].success() && out[0].distanceMeters() == 480 && out[0]
            .routePoints()
            .size() == 2, "route output");
    }
}
