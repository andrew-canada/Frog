package database.route;

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

import org.bson.Document;
import org.bson.json.JsonParseException;

import entity.GeoPoint;
import entity.Route;
import use_case.port.RouteGateway;

/**
 * Live GraphHopper Routing API adapter. The API key is supplied at the composition root.
 */
public final class GraphhopperRouteDataAccessObject implements RouteGateway {
    public static final String API_KEY_ENV = "GRAPHHOPPER_API_KEY";
    private static final int HTTP_REDIRECTION_STATUS = 300;
    private static final int HTTP_SUCCESS_STATUS = 200;
    private static final int REQUEST_TIMEOUT_SECONDS = 20;
    private static final double MILLISECONDS_PER_SECOND = 1000.0;
    private static final int CONNECTION_TIMEOUT_SECONDS = 10;
    private static final URI DEFAULT_ENDPOINT = URI.create("https://graphhopper.com/api/1/route");

    private final HttpClient httpClient;
    private final URI endpoint;
    private final String apiKey;

    public GraphhopperRouteDataAccessObject(final String apiKey) {
        this(HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS))
            .build(), DEFAULT_ENDPOINT, apiKey);
    }

    public GraphhopperRouteDataAccessObject(final HttpClient httpClient, final URI endpoint, final String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Configure " + API_KEY_ENV + " before starting FlushID.");
        }
        this.httpClient = httpClient;
        this.endpoint = endpoint;
        this.apiKey = apiKey;
    }

    private static Route parseRoute(final String json) {
        final Document root;
        try {
            root = Document.parse(json);
        }
        catch (final JsonParseException malformed) {
            throw new IllegalStateException("GraphHopper returned invalid JSON.", malformed);
        }
        final List<Document> paths = root.getList("paths", Document.class);
        if (paths == null || paths.isEmpty()) {
            throw new IllegalStateException("GraphHopper returned no walking route.");
        }
        final Document path = paths.getFirst();
        final Document geometry = path.get("points", Document.class);
        if (geometry == null) {
            throw new IllegalStateException("GraphHopper response did not contain route geometry.");
        }
        final List<?> coordinates = geometry.getList("coordinates", List.class);
        final List<GeoPoint> points = routePoints(coordinates);
        final Number distance = path.get("distance", Number.class);
        final Number time = path.get("time", Number.class);
        int distanceMeters = 0;
        if (distance != null) {
            distanceMeters = (int) Math.round(distance.doubleValue());
        }
        int timeSeconds = 0;
        if (time != null) {
            timeSeconds = (int) Math.round(time.doubleValue() / MILLISECONDS_PER_SECOND);
        }
        return new Route(points, distanceMeters, timeSeconds);
    }

    private static List<GeoPoint> routePoints(final List<?> coordinates) {
        final List<GeoPoint> points = new ArrayList<>();
        if (coordinates != null) {
            for (final Object coordinate : coordinates) {
                if (coordinate instanceof final List<?> pair && pair.size() >= 2 && pair.get(
                    0) instanceof final Number longitude && pair.get(1) instanceof final Number latitude) {
                    points.add(new GeoPoint(latitude.doubleValue(), longitude.doubleValue()));
                }
            }
        }
        if (points.size() < 2) {
            throw new IllegalStateException("GraphHopper returned incomplete route geometry.");
        }
        return points;
    }

    private static String point(final GeoPoint point) {
        return String.format(Locale.ROOT, "%.7f,%.7f", point.latitude(), point.longitude());
    }

    @Override
    public Route getRoute(final GeoPoint from, final GeoPoint to) {
        final URI requestUri = URI.create(endpoint + "?point=" + point(from) + "&point=" + point(to)
            + "&profile=foot&locale=en&instructions=false&calc_points=true&points_encoded=false&key="
            + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
        final HttpRequest request = HttpRequest
            .newBuilder(requestUri)
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .header("Accept", "application/json")
            .header("User-Agent", "FlushID/1.0")
            .GET()
            .build();
        try {
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < HTTP_SUCCESS_STATUS || response.statusCode() >= HTTP_REDIRECTION_STATUS) {
                throw new IllegalStateException("GraphHopper returned HTTP " + response.statusCode() + ".");
            }
            return parseRoute(response.body());
        }
        catch (final InterruptedException interrupted) {
            Thread
                .currentThread()
                .interrupt();
            throw new IllegalStateException("GraphHopper request was interrupted.", interrupted);
        }
        catch (final IOException failure) {
            throw new IllegalStateException("Could not reach the GraphHopper routing service.", failure);
        }
    }
}
