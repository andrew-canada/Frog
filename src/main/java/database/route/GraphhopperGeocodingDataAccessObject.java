package database.route;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.bson.Document;
import org.bson.json.JsonParseException;

import entity.GeoPoint;
import use_case.port.AddressLookupGateway;

/**
 * Live GraphHopper forward-geocoding adapter used for address-based map origins.
 */
public final class GraphhopperGeocodingDataAccessObject implements AddressLookupGateway {
    private static final int MAGIC_300 = 300;
    private static final int MAGIC_200 = 200;
    private static final int MAGIC_20 = 20;
    private static final int MAGIC_10 = 10;
    private static final URI DEFAULT_ENDPOINT = URI.create("https://graphhopper.com/api/1/geocode");
    private final HttpClient httpClient;
    private final URI endpoint;
    private final String apiKey;

    public GraphhopperGeocodingDataAccessObject(final String apiKey) {
        this(HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(MAGIC_10))
            .build(), DEFAULT_ENDPOINT, apiKey);
    }

    public GraphhopperGeocodingDataAccessObject(final HttpClient httpClient, final URI endpoint, final String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Set the " + GraphhopperRouteDataAccessObject.API_KEY_ENV
                + " environment variable before starting FlushID.");
        }
        this.httpClient = httpClient;
        this.endpoint = endpoint;
        this.apiKey = apiKey;
    }

    private static GeoPoint parsePoint(final String json) {
        final Document root;
        try {
            root = Document.parse(json);
        }
        catch (final JsonParseException malformed) {
            throw new IllegalStateException("GraphHopper returned invalid address data.", malformed);
        }
        final List<Document> hits = root.getList("hits", Document.class);
        if (hits == null || hits.isEmpty()) {
            throw new IllegalArgumentException("No location matched that address.");
        }
        final Document point = hits
            .getFirst()
            .get("point", Document.class);
        final Number latitude;
        if (point == null) {
            latitude = null;
        }
        else {
            latitude = point.get("lat", Number.class);
        }
        final Number longitude;
        if (point == null) {
            longitude = null;
        }
        else {
            longitude = point.get("lng", Number.class);
        }
        if (latitude == null || longitude == null) {
            throw new IllegalStateException("GraphHopper returned incomplete address data.");
        }
        return new GeoPoint(latitude.doubleValue(), longitude.doubleValue());
    }

    @Override
    public GeoPoint lookup(final String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Enter an address to search.");
        }
        final URI requestUri = URI.create(
            endpoint + "?q=" + URLEncoder.encode(address.trim(), StandardCharsets.UTF_8) + "&limit=1&locale=en&key="
                + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
        final HttpRequest request = HttpRequest
            .newBuilder(requestUri)
            .timeout(Duration.ofSeconds(MAGIC_20))
            .header("Accept", "application/json")
            .header("User-Agent", "FlushID/1.0")
            .GET()
            .build();
        try {
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < MAGIC_200 || response.statusCode() >= MAGIC_300) {
                throw new IllegalStateException("GraphHopper returned HTTP " + response.statusCode() + ".");
            }
            return parsePoint(response.body());
        }
        catch (final InterruptedException interrupted) {
            Thread
                .currentThread()
                .interrupt();
            throw new IllegalStateException("Address search was interrupted.", interrupted);
        }
        catch (final IOException failure) {
            throw new IllegalStateException("Could not reach GraphHopper address search.", failure);
        }
    }
}
