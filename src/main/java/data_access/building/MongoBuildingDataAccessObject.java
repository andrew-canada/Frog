package data_access.building;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import data_access.MongoDocuments;
import entity.Building;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/** Additive adapter for the application's compact building entity. */
public final class MongoBuildingDataAccessObject {
    private final MongoCollection<Document> buildings;

    public MongoBuildingDataAccessObject(MongoDatabase database) {
        buildings = database.getCollection("Buildings");
    }

    /** Idempotently persists campus reference data, while retaining other team-owned fields. */
    public List<Building> ensureLocations(List<Building> locations) {
        List<Building> persisted = new ArrayList<>();
        for (Building location : locations) {
            Document geoPoint = new Document("type", "Point")
                    .append("coordinates", List.of(location.longitude(), location.latitude()));
            buildings.updateOne(Filters.eq("buildingCode", location.code()),
                    Updates.combine(
                            Updates.set("longName", location.name()),
                            Updates.set("location", geoPoint),
                            Updates.setOnInsert("shortName", location.name()),
                            Updates.setOnInsert("controlInfo", "U of T St. George campus reference location")
                    ), new UpdateOptions().upsert(true));

            Document document = buildings.find(Filters.eq("buildingCode", location.code())).first();
            if (document != null) persisted.add(toEntity(document));
        }
        return List.copyOf(persisted);
    }

    private static Building toEntity(Document document) {
        Document location = document.get("location", Document.class);
        List<?> coordinates = location == null
                ? List.of()
                : location.getList("coordinates", Object.class, List.of());
        double longitude = coordinates.size() > 0 && coordinates.get(0) instanceof Number number
                ? number.doubleValue() : MongoDocuments.number(document, 0, "longitude", "lng");
        double latitude = coordinates.size() > 1 && coordinates.get(1) instanceof Number number
                ? number.doubleValue() : MongoDocuments.number(document, 0, "latitude", "lat");
        String code = MongoDocuments.string(document, MongoDocuments.id(document), "buildingCode", "code");
        String name = MongoDocuments.string(document, code, "longName", "shortName", "name");
        return new Building(code, name, latitude, longitude);
    }
}
