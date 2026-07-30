package data_access.washroom;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import data_access.MongoDocuments;
import entity.Building;
import entity.ReviewSummary;
import entity.Washroom;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class MongoWashroomDataAccessObject implements use_case.gateway.WashroomDataAccessInterface {
    private final MongoCollection<Document> washrooms;
    private final MongoCollection<Document> buildings;
    private final MongoCollection<Document> reviews;

    public MongoWashroomDataAccessObject(MongoDatabase database) {
        washrooms = database.getCollection("Washrooms");
        buildings = database.getCollection("Buildings");
        reviews = database.getCollection("Reviews");
    }

    /** Idempotently creates one selectable washroom per campus landmark. */
    public void ensureCampusWashrooms(List<Building> campusBuildings) {
        for (Building building : campusBuildings) {
            Document buildingDocument = buildings.find(Filters.eq("buildingCode", building.code())).first();
            if (buildingDocument == null) continue;
            Object buildingId = buildingDocument.get("_id");
            String seedKey = "campus-" + building.code().toLowerCase(Locale.ROOT) + "-washroom";
            washrooms.updateOne(Filters.eq("seedKey", seedKey),
                    Updates.combine(
                            Updates.set("seedKey", seedKey),
                            Updates.set("buildingID", buildingId),
                            Updates.set("buildingCode", building.code()),
                            Updates.set("name", building.name()),
                            Updates.set("floor", "Main floor"),
                            Updates.set("gender", "ALL_GENDER"),
                            Updates.set("accessible", false),
                            Updates.set("accessibility", false),
                            Updates.set("numToilets", 0),
                            Updates.set("numSinks", 0),
                            Updates.set("locationDescription", "Main-floor washroom location at " + building.name())
                    ), new UpdateOptions().upsert(true));
        }
    }

    @Override public Optional<Washroom> getById(String id) {
        Document document = MongoDocuments.findById(washrooms, id);
        return document == null ? Optional.empty() : Optional.of(toEntity(document));
    }

    @Override public List<Washroom> getAll() {
        List<Washroom> result = new ArrayList<>();
        for (Document document : washrooms.find()) result.add(toEntity(document));
        return List.copyOf(result);
    }

    @Override public List<Washroom> getNearby(double latitude, double longitude, double radiusMeters) {
        return getAll().stream().filter(washroom -> distance(latitude, longitude,
                washroom.building().latitude(), washroom.building().longitude()) <= radiusMeters).toList();
    }

    private Washroom toEntity(Document document) {
        String id = MongoDocuments.id(document);
        String buildingId = MongoDocuments.string(document, "", "buildingID", "buildingId");
        Document buildingDocument = MongoDocuments.findById(buildings, buildingId);
        if (buildingDocument == null) {
            String buildingCode = MongoDocuments.string(document, buildingId, "buildingCode");
            buildingDocument = buildings.find(new Document("buildingCode", buildingCode)).first();
        }
        if (buildingDocument == null) {
            throw new IllegalStateException("Washroom " + id + " references a missing building.");
        }
        Building building = toBuilding(buildingDocument);
        String floor = MongoDocuments.string(document, "Unknown floor", "floor");
        String name = MongoDocuments.string(document, building.name() + ", " + floor, "name");
        Washroom.Gender gender = parseGender(MongoDocuments.string(document, "ALL_GENDER", "gender"));
        return new Washroom(id, name, building, floor,
                MongoDocuments.bool(document, false, "accessible"), gender,
                Math.max(0, MongoDocuments.integer(document, 0, "numToilets", "toilets")),
                Math.max(0, MongoDocuments.integer(document, 0, "numSinks", "sinks")),
                MongoDocuments.string(document, "Inside " + building.name(), "locationDescription", "description"),
                summary(id));
    }

    private ReviewSummary summary(String washroomId) {
        double rating = 0, cleanliness = 0; int count = 0;
        for (Document review : reviews.find()) {
            if (!MongoDocuments.referenceMatches(review.get("washroomID"), washroomId)
                    && !MongoDocuments.referenceMatches(review.get("washroomId"), washroomId)) continue;
            rating += clampRating(MongoDocuments.number(review, 0, "rating", "stars"));
            cleanliness += clampRating(MongoDocuments.number(review,
                    MongoDocuments.number(review, 0, "rating", "stars"), "cleanliness"));
            count++;
        }
        return count == 0 ? ReviewSummary.empty() : new ReviewSummary(rating / count, cleanliness / count, count);
    }

    private static Building toBuilding(Document document) {
        Document location = document.get("location", Document.class);
        List<?> coordinates = location == null ? List.of() : location.getList("coordinates", Object.class, List.of());
        double longitude = coordinates.size() > 0 && coordinates.get(0) instanceof Number number
                ? number.doubleValue() : MongoDocuments.number(document, 0, "longitude", "lng");
        double latitude = coordinates.size() > 1 && coordinates.get(1) instanceof Number number
                ? number.doubleValue() : MongoDocuments.number(document, 0, "latitude", "lat");
        String code = MongoDocuments.string(document, MongoDocuments.id(document), "buildingCode", "code");
        String name = MongoDocuments.string(document, code, "longName", "shortName", "name");
        return new Building(code, name, latitude, longitude);
    }

    private static Washroom.Gender parseGender(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        try { return Washroom.Gender.valueOf(normalized); }
        catch (IllegalArgumentException ignored) { return Washroom.Gender.ALL_GENDER; }
    }

    private static double clampRating(double value) { return Math.max(1, Math.min(5, value)); }
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double y = Math.toRadians(lat2 - lat1);
        return Math.sqrt(x * x + y * y) * 6_371_000;
    }
}
