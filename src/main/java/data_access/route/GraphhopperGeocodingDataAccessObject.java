package data_access.route;

import entity.GeoPoint;
import org.bson.Document;
import use_case.port.AddressLookupGateway;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Live GraphHopper forward-geocoding adapter used for address-based map origins.
 */
public final class GraphhopperGeocodingDataAccessObject implements AddressLookupGateway {
    private static final URI DEFAULT_ENDPOINT = URI.create("https://graphhopper.com/api/1/geocode");
    private final HttpClient httpClient;
    private final URI endpoint;
    private final String apiKey;

    public GraphhopperGeocodingDataAccessObject(String apiKey) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), DEFAULT_ENDPOINT, apiKey);
    }

    public GraphhopperGeocodingDataAccessObject(HttpClient httpClient, URI endpoint, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Set the " + GraphhopperRouteDataAccessObject.API_KEY_ENV + " environment variable before starting FlushID.");
        }
        this.httpClient = httpClient;
        this.endpoint = endpoint;
        this.apiKey = apiKey;
    }

    private static GeoPoint parsePoint(String json) {
        Document root;
        try {
            root = Document.parse(json);
        } catch (RuntimeException malformed) {
            throw new IllegalStateException("GraphHopper returned invalid address data.", malformed);
        }
        List<Document> hits = root.getList("hits", Document.class);
        if (hits == null || hits.isEmpty()) throw new IllegalArgumentException("No location matched that address.");
        Document point = hits.getFirst().get("point", Document.class);
        Number latitude = point == null ? null : point.get("lat", Number.class);
        Number longitude = point == null ? null : point.get("lng", Number.class);
        if (latitude == null || longitude == null)
            throw new IllegalStateException("GraphHopper returned incomplete address data.");
        return new GeoPoint(latitude.doubleValue(), longitude.doubleValue());
    }

    @Override
    public GeoPoint lookup(String address) {
        if (address == null || address.isBlank()) throw new IllegalArgumentException("Enter an address to search.");
        URI requestUri = URI.create(endpoint + "?q=" + URLEncoder.encode(address.trim(), StandardCharsets.UTF_8)
                + "&limit=1&locale=en&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(requestUri).timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json").header("User-Agent", "FlushID/1.0").GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("GraphHopper returned HTTP " + response.statusCode() + ".");
            }
            return parsePoint(response.body());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Address search was interrupted.", interrupted);
        } catch (IOException failure) {
            throw new IllegalStateException("Could not reach GraphHopper address search.", failure);
        }
    }
}
