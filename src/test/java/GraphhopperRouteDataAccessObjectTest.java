import com.sun.net.httpserver.HttpServer;
import database.route.GraphhopperRouteDataAccessObject;
import entity.GeoPoint;
import entity.Route;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

final class GraphhopperRouteDataAccessObjectTest {
    static void run() {
        HttpServer server = null;
        try {
            final AtomicReference<String> query = new AtomicReference<>();
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/route", exchange -> {
                query.set(exchange
                    .getRequestURI()
                    .getRawQuery());
                final byte[] body = ("{\"paths\":[{\"distance\":480.6,\"time\":361000,"
                    + "\"points\":{\"type\":\"LineString\",\"coordinates\":["
                    + "[-79.3957,43.6629],[-79.3960,43.6615],[-79.3974,43.6597]]}}]}")
                    .getBytes(StandardCharsets.UTF_8);
                exchange
                    .getResponseHeaders()
                    .add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange
                    .getResponseBody()
                    .write(body);
                exchange.close();
            });
            server.start();
            final URI endpoint = URI.create("http://127.0.0.1:" + server
                .getAddress()
                .getPort() + "/route");
            final var adapter = new GraphhopperRouteDataAccessObject(HttpClient.newHttpClient(), endpoint, "test-key");
            final Route route = adapter.getRoute(new GeoPoint(43.6629, -79.3957), new GeoPoint(43.6597, -79.3974));
            TestSupport.check(route.distanceMeters() == 481, "distance should be parsed and rounded");
            TestSupport.check(route.timeSeconds() == 361, "GraphHopper milliseconds should become seconds");
            TestSupport.check(route
                .points()
                .size() == 3, "route geometry should be parsed");
            TestSupport.check(query
                .get()
                .contains("profile=foot"), "walking profile must be requested");
            TestSupport.check(query
                .get()
                .contains("points_encoded=false"), "GeoJSON coordinates must be requested");
        } catch (final Exception failure) {
            throw new AssertionError("GraphHopper adapter test failed", failure);
        } finally {
            if (server != null) server.stop(0);
        }
    }
}
