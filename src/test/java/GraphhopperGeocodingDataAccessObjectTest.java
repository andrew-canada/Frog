import com.sun.net.httpserver.HttpServer;
import database.route.GraphhopperGeocodingDataAccessObject;
import entity.GeoPoint;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

final class GraphhopperGeocodingDataAccessObjectTest {
    static void run() {
        HttpServer server = null;
        try {
            final AtomicReference<String> query = new AtomicReference<>();
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/geocode", exchange -> {
                query.set(exchange
                    .getRequestURI()
                    .getRawQuery());
                final byte[] body =
                    "{\"hits\":[{\"point\":{\"lat\":43.6629,\"lng\":-79.3957}}]}".getBytes(StandardCharsets.UTF_8);
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
                .getPort() + "/geocode");
            final var adapter = new GraphhopperGeocodingDataAccessObject(HttpClient.newHttpClient(), endpoint, "test-key");
            final GeoPoint point = adapter.lookup("University of Toronto");
            TestSupport.check(point.latitude() == 43.6629 && point.longitude() == -79.3957,
                "geocoding should parse the first hit");
            TestSupport.check(query
                .get()
                .contains("q=University+of+Toronto"), "address query must be encoded");
            TestSupport.check(query
                .get()
                .contains("limit=1"), "only the best geocoding result should be requested");
        } catch (final Exception failure) {
            throw new AssertionError("GraphHopper geocoding adapter test failed", failure);
        } finally {
            if (server != null) server.stop(0);
        }
    }
}
