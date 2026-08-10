import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import database.route.GraphhopperGeocodingDataAccessObject;
import database.route.GraphhopperRouteDataAccessObject;
import entity.GeoPoint;
import entity.Route;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;

/** Verifies deterministic HTTP failure and malformed-payload handling for route gateways. */
class RouteAdapterEdgeCoverageTest {
    @Test
    void routeAdapterRejectsConfigurationAndBadResponses() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new GraphhopperRouteDataAccessObject(""));
        assertThrows(IllegalArgumentException.class, () -> new GraphhopperRouteDataAccessObject(null));
        final GeoPoint from = new GeoPoint(43.66, -79.39);
        final GeoPoint to = new GeoPoint(43.67, -79.40);
        assertThrows(IllegalStateException.class, () -> withResponse(500, "{}", endpoint ->
            new GraphhopperRouteDataAccessObject(HttpClient.newHttpClient(), endpoint, "key").getRoute(from, to)));
        assertThrows(IllegalStateException.class, () -> withResponse(200, "not json", endpoint ->
            new GraphhopperRouteDataAccessObject(HttpClient.newHttpClient(), endpoint, "key").getRoute(from, to)));
        assertThrows(IllegalStateException.class, () -> withResponse(200, "{\"paths\":[]}", endpoint ->
            new GraphhopperRouteDataAccessObject(HttpClient.newHttpClient(), endpoint, "key").getRoute(from, to)));
        assertThrows(IllegalStateException.class, () -> withResponse(200,
            "{\"paths\":[{\"points\":{\"coordinates\":[[-79.39,43.66]]}}]}", endpoint ->
                new GraphhopperRouteDataAccessObject(HttpClient.newHttpClient(), endpoint, "key")
                    .getRoute(from, to)));
        final Route defaults = withResponse(200,
            "{\"paths\":[{\"points\":{\"coordinates\":[[-79.39,43.66],[-79.40,43.67]]}}]}", endpoint ->
                new GraphhopperRouteDataAccessObject(HttpClient.newHttpClient(), endpoint, "key")
                    .getRoute(from, to));
        assertEquals(0, defaults.distanceMeters());
        assertEquals(0, defaults.timeSeconds());
    }

    @Test
    void geocodingAdapterRejectsConfigurationAndBadResponses() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new GraphhopperGeocodingDataAccessObject(""));
        assertThrows(IllegalArgumentException.class, () -> new GraphhopperGeocodingDataAccessObject(null));
        final GraphhopperGeocodingDataAccessObject adapter = new GraphhopperGeocodingDataAccessObject(
            HttpClient.newHttpClient(), URI.create("http://127.0.0.1:1/geocode"), "key");
        assertThrows(IllegalArgumentException.class, () -> adapter.lookup(null));
        assertThrows(IllegalArgumentException.class, () -> adapter.lookup("  "));
        assertThrows(IllegalStateException.class, () -> withResponse(404, "{}", endpoint ->
            new GraphhopperGeocodingDataAccessObject(HttpClient.newHttpClient(), endpoint, "key")
                .lookup("address")));
        assertThrows(IllegalArgumentException.class, () -> withResponse(200, "{\"hits\":[]}", endpoint ->
            new GraphhopperGeocodingDataAccessObject(HttpClient.newHttpClient(), endpoint, "key")
                .lookup("address")));
        assertThrows(IllegalStateException.class, () -> withResponse(200, "not json", endpoint ->
            new GraphhopperGeocodingDataAccessObject(HttpClient.newHttpClient(), endpoint, "key")
                .lookup("address")));
        assertThrows(IllegalStateException.class, () -> withResponse(200, "{\"hits\":[{}]}", endpoint ->
            new GraphhopperGeocodingDataAccessObject(HttpClient.newHttpClient(), endpoint, "key")
                .lookup("address")));
        final GeoPoint point = withResponse(200, "{\"hits\":[{\"point\":{\"lat\":43.66,\"lng\":-79.39}}]}",
            endpoint -> new GraphhopperGeocodingDataAccessObject(HttpClient.newHttpClient(), endpoint, "key")
                .lookup("address"));
        assertEquals(43.66, point.latitude());
    }

    private static <T> T withResponse(final int status, final String body, final EndpointAction<T> action)
        throws Exception {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(status, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            });
            server.start();
            final URI endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api");
            return action.apply(endpoint);
        }
        finally {
            if (server != null) {
                server.stop(0);
            }
        }
    }

    @FunctionalInterface
    private interface EndpointAction<T> {
        T apply(URI endpoint) throws Exception;
    }
}
