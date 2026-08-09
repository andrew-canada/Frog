package data_access.route;

import entity.GeoPoint;
import entity.Route;
import org.bson.Document;
import use_case.port.RouteGateway;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Live GraphHopper Routing API adapter. The API key is supplied at the composition root.
 */
public final class GraphhopperRouteDataAccessObject implements RouteGateway {
    public static final String API_KEY_ENV = "GRAPHHOPPER_API_KEY";
    private static final URI DEFAULT_ENDPOINT = URI.create("https://graphhopper.com/api/1/route");

    private final HttpClient httpClient;
    private final URI endpoint;
    private final String apiKey;

    public GraphhopperRouteDataAccessObject(String apiKey) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), DEFAULT_ENDPOINT, apiKey);
    }

    public GraphhopperRouteDataAccessObject(HttpClient httpClient, URI endpoint, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Set the " + API_KEY_ENV + " environment variable before starting FlushID.");
        }
        this.httpClient = httpClient;
        this.endpoint = endpoint;
        this.apiKey = apiKey;
    }

    private static Route parseRoute(String json) {
        Document root;
        try {
            root = Document.parse(json);
        } catch (RuntimeException malformed) {
            throw new IllegalStateException("GraphHopper returned invalid JSON.", malformed);
        }
        List<Document> paths = root.getList("paths", Document.class);
        if (paths == null || paths.isEmpty()) {
            throw new IllegalStateException("GraphHopper returned no walking route.");
        }
        Document path = paths.getFirst();
        Document geometry = path.get("points", Document.class);
        if (geometry == null) throw new IllegalStateException("GraphHopper response did not contain route geometry.");
        List<?> coordinates = geometry.getList("coordinates", List.class);
        List<GeoPoint> points = new ArrayList<>();
        if (coordinates != null) for (Object coordinate : coordinates) {
            if (coordinate instanceof List<?> pair && pair.size() >= 2
                    && pair.get(0) instanceof Number longitude && pair.get(1) instanceof Number latitude) {
                points.add(new GeoPoint(latitude.doubleValue(), longitude.doubleValue()));
            }
        }
        if (points.size() < 2) throw new IllegalStateException("GraphHopper returned incomplete route geometry.");
        Number distance = path.get("distance", Number.class);
        Number time = path.get("time", Number.class);
        return new Route(points, distance == null ? 0 : (int) Math.round(distance.doubleValue()),
                time == null ? 0 : (int) Math.round(time.doubleValue() / 1000.0));
    }

    private static String point(GeoPoint point) {
        return String.format(Locale.ROOT, "%.7f,%.7f", point.latitude(), point.longitude());
    }

    @Override
    public Route getRoute(GeoPoint from, GeoPoint to) {
        URI requestUri = URI.create(endpoint + "?point=" + point(from) + "&point=" + point(to)
                + "&profile=foot&locale=en&instructions=false&calc_points=true&points_encoded=false&key="
                + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(requestUri).timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json").header("User-Agent", "FlushID/1.0").GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("GraphHopper returned HTTP " + response.statusCode() + ".");
            }
            return parseRoute(response.body());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("GraphHopper request was interrupted.", interrupted);
        } catch (IOException failure) {
            throw new IllegalStateException("Could not reach the GraphHopper routing service.", failure);
        }
    }
}
