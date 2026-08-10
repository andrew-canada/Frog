package database.building;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import database.AbstractCondition;
import database.Condition;
import database.DBDataAccessObject;
import database.MongoDocuments;
import database.Operator;
import entity.Building;

public class DBBuildingDataAccessObject extends DBDataAccessObject {
    private static final String FIELD_BUILDINGCODE = "buildingCode";
    private static final String FIELD_SHORTNAME = "shortName";
    private static final String FIELD_LONGNAME = "longName";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_CONTROLINFO = "controlInfo";
    private static final String FIELD_COORDINATES = "coordinates";

    private static final List<String> ALLOWED_ATTRIBUTES =
        List.of(new String[] {FIELD_BUILDINGCODE, FIELD_SHORTNAME, FIELD_LONGNAME, FIELD_LOCATION, FIELD_CONTROLINFO});
    private final MongoCollection<Document> collection;

    public DBBuildingDataAccessObject() {
        collection = database.getCollection("Buildings");
    }

    public DBBuildingDataAccessObject(final MongoDatabase database) {
        super(database);
        collection = database.getCollection("Buildings");
    }

    /**
     * Returns all buildings who satisfy all the given conditions.
     *
     * @param conditions a list of condition objects that the returned buildings must satisfy
     * @return The buildings that match all the conditions
     */
    private List<entity.Building> getMatching(final Iterable<AbstractCondition<?>> conditions) {
        return new ArrayList<>(getMatchingIDMap(conditions).values());

    }

    /**
     * Returns all buildings who satisfy the condition.
     *
     * @param condition a condition object that the returned buildings must satisfy
     * @return The buildings that match the conditions
     */
    public List<entity.Building> getMatching(final AbstractCondition<?> condition) {

        final List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        return getMatching(conditions);

    }

    /**
     * Returns all buildings which satisfy all the given conditions, with database IDs.
     *
     * @param conditions a list of condition objects that the returned buildings must satisfy
     * @return The buildings that match all the conditions mapped to their IDs in the database.
     */
    private Map<String, entity.Building> getMatchingIDMap(final Iterable<AbstractCondition<?>> conditions) {

        checkAttribute(conditions);

        final Bson filter = parseConditions(conditions);
        final List<Document> docs = getAll(filter);

        final Map<String, entity.Building> buildings = new HashMap<>();
        for (final Document doc : docs) {
            final entity.Building building = createBuilding(doc);
            buildings.put(MongoDocuments.id(doc), building);
        }
        return buildings;

    }

    /**
     * Returns all buildings which satisfy the given condition, with database IDs.
     *
     * @param condition a condition object that the returned buildings must satisfy
     * @return The buildings that match the condition mapped to their IDs in the database.
     */
    public Map<String, entity.Building> getMatchingIDMap(final AbstractCondition<?> condition) {

        final List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        return getMatchingIDMap(conditions);

    }

    /**
     * Parses a list of AbstractCondition objects into a single Bson filter.
     *
     * @param conditions list of condition objects to be connected by and statements
     * @return a Bson filter representing satisfying all conditions
     */
    private static Bson parseConditions(final Iterable<AbstractCondition<?>> conditions) {
        final List<Bson> filters = new ArrayList<>();
        conditions.forEach(condition -> {
            filters.add(condition.getFilter());
        });
        final Bson result;
        if (filters.isEmpty()) {
            result = new Document();
        }
        else {
            result = Filters.and(filters);
        }
        return result;
    }

    /**
     * Return a list of Documents which match the specified parameters.
     *
     * @param filter the filter that must be satisfied for the Document to be returned
     * @return The list of valid documents
     */
    private List<Document> getAll(final Bson filter) {
        final List<Document> docs = new ArrayList<>();
        return collection
            .find(filter)
            .into(docs);
    }

    /**
     * Creates a Building object out of the inputted Document.
     *
     * @param doc Document containing building data for a specific building
     * @return the Building object constructed using that data
     */
    private static entity.Building createBuilding(final Document doc) {
        return new entity.Building(MongoDocuments.string(doc, MongoDocuments.id(doc), FIELD_BUILDINGCODE, "code"),
            MongoDocuments.string(doc, "Unknown building", FIELD_LONGNAME, FIELD_SHORTNAME, "name"), getLatitude(doc),
            getLongitude(doc));
    }

    /**
     * Parse Latitude and Longitude of a Building into a Document.
     *
     * @param building Building object with longitude and latitude
     * @return Document that can be inserted into the database and used for geospatial filters
     */
    private static Document createLocation(final Building building) {
        final Document location = new Document("type", "Point");
        final List<Double> coords = new ArrayList<>();
        coords.add(0, building.getLongitude());
        coords.add(1, building.getLatitude());
        location.append(FIELD_COORDINATES, coords);
        return location;
    }

    /**
     * Return the longitude as a double parsed from a Document containing.
     * geospatial data
     *
     * @param doc Document of valid format:
     *            type: "Point"
     *            coordinates: [longitude, latitude]
     * @return Double representing longitude
     */
    private static double getLongitude(final Document doc) {
        final Document location = doc.get(FIELD_LOCATION, Document.class);
        final List<?> coordinates;
        if (location == null) {
            coordinates = List.of();
        }
        else {
            coordinates = location.getList(FIELD_COORDINATES, Object.class, List.of());
        }
        final double longitude;
        if (coordinates.size() > 0 && coordinates.get(0) instanceof final Number coordinate) {
            longitude = coordinate.doubleValue();
        }
        else {
            longitude = MongoDocuments.number(doc, 0, "longitude", "lng");
        }
        return longitude;
    }

    /**
     * Return the latitude as a double parsed from a Document containing.
     * geospatial data
     *
     * @param doc Document of valid format:
     *            {type: "Point"
     *            coordinates: [longitude, latitude]}
     * @return Double representing latitude
     */
    private static double getLatitude(final Document doc) {
        final Document location = doc.get(FIELD_LOCATION, Document.class);
        final List<?> coordinates;
        if (location == null) {
            coordinates = List.of();
        }
        else {
            coordinates = location.getList(FIELD_COORDINATES, Object.class, List.of());
        }
        final double latitude;
        if (coordinates.size() > 1 && coordinates.get(1) instanceof final Number coordinate) {
            latitude = coordinate.doubleValue();
        }
        else {
            latitude = MongoDocuments.number(doc, 0, "latitude", "lat");
        }
        return latitude;
    }

    /**
     * Checks the condition against the list of allowed attributes, throwing a
     * runtime exception if it's not a valid attribute.
     *
     * @param condition The condition to check.
     * @throws RuntimeException if the attribute is not allowed.
     */
    private static void checkAttribute(final AbstractCondition<?> condition) {
        if (!ALLOWED_ATTRIBUTES.contains(condition.getFieldName())) {
            throw new RuntimeException("Not a valid attribute");
        }
    }

    /**
     * Checks the conditions against the list of allowed attributes, throwing a
     * runtime exception if it's not a valid attribute.
     *
     * @param conditions The conditions to check.
     */
    private static void checkAttribute(final Iterable<AbstractCondition<?>> conditions) {
        for (final AbstractCondition<?> condition : conditions) {
            checkAttribute(condition);
        }
    }

    /**
     * Performs this operation.
     *
     * @param filename parameter value.
     *
     * @return the operation result.
     *
     * @throws Exception if the operation fails.
     */
    public static List<Building> loadBuildings(final String filename) throws Exception {
        final List<Building> buildings = new ArrayList<>();

        try (Reader reader = new FileReader(filename); JsonReader jsonReader = Json.createReader(reader)) {

            final JsonArray jsonArray = jsonReader.readArray();

            for (final JsonValue value : jsonArray) {
                final JsonObject obj = (JsonObject) value;

                final String id = obj.getString("ID");
                final String name = obj.getString("name");

                // Stored as strings in the JSON.

                final double latitude = Double.parseDouble(obj.getString("latitude"));
                final double longitude = Double.parseDouble(obj.getString("longitude"));

                buildings.add(new entity.Building(id, name, latitude, longitude));
            }
        }

        return buildings;
    }

    /**
     * Main method that demonstrates a building query.
     *
     * @param args parameter value.
     *
     * @throws FileNotFoundException if the operation fails.
     */
    public static void main(final String[] args) throws FileNotFoundException {
        final DBBuildingDataAccessObject buildingDataAccessObject = new DBBuildingDataAccessObject();
        final Condition condition = new Condition<>(FIELD_BUILDINGCODE, Operator.NE, "00");
        System.out.println(buildingDataAccessObject.getMatching(condition));

    }

    /**
     * Writes a single Building object to the database. Latitude and Longitude are converted
     * to a location object for geospatial filtering
     *
     * @param building The Building object to be written.
     * @return the ID for the written object.
     */
    private String write(final Building building) {
        final Document doc = new Document();
        doc.append(FIELD_BUILDINGCODE, building.getBuildingCode());
        doc.append(FIELD_SHORTNAME, building.getBuildingNameShort());
        doc.append(FIELD_LONGNAME, building.getBuildingNameLong());
        doc.append(FIELD_LOCATION, createLocation(building));
        doc.append(FIELD_CONTROLINFO, building.getControlInfo());

        return collection
            .insertOne(doc)
            .getInsertedId()
            .toString();
    }

    /**
     * Deletes every entry in the database that matches the given conditions.
     *
     * @param conditions List of AbstractCondition objects. An object must satisfy
     *                   all conditions to be deleted
     */
    private void delete(final Iterable<AbstractCondition<?>> conditions) {
        checkAttribute(conditions);

        final Bson filter = parseConditions(conditions);
        collection.deleteMany(filter);
    }

    /**
     * Deletes every entry in the database that matches the given condition.
     *
     * @param condition A AbstractCondition object that the object must satisfy.
     */
    public void delete(final AbstractCondition<?> condition) {
        final List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        delete(conditions);
    }

    /**
     * Production campus-location seed used by the current Swing application.
     * @param locations parameter value.
     * @return the operation result.
     */
    public List<entity.Building> ensureLocations(final List<entity.Building> locations) {
        final List<entity.Building> persisted = new ArrayList<>();
        for (final entity.Building location : locations) {
            final List<entity.Building> existing =
                getMatching(new Condition<>(FIELD_BUILDINGCODE, Operator.EQ, location.code()));
            if (existing.isEmpty()) {
                write(location);
            }
            final Document geoPoint =
                new Document("type", "Point").append(FIELD_COORDINATES, List.of(location.longitude(),
                    location.latitude()));
            collection.updateOne(Filters.eq(FIELD_BUILDINGCODE, location.code()),
                Updates.combine(Updates.set(FIELD_LONGNAME, location.name()), Updates.set(FIELD_LOCATION, geoPoint),
                    Updates.setOnInsert(FIELD_SHORTNAME, location.name()),
                    Updates.setOnInsert(FIELD_CONTROLINFO, "U of T St. George campus reference location")),
                new UpdateOptions().upsert(true));
            final List<entity.Building> refreshed =
                getMatching(new Condition<>(FIELD_BUILDINGCODE, Operator.EQ, location.code()));
            if (!refreshed.isEmpty()) {
                persisted.add(refreshed.getFirst());
            }
        }
        return List.copyOf(persisted);
    }
}
