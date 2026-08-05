package data_access.building;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import data_access.Condition;
import data_access.AbstractCondition;
import data_access.MongoDocuments;
import data_access.Operator;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import entity.Building;
import data_access.DBDataAccessObject;

import java.io.*;
import java.util.*;
import javax.json.*;

public class DBBuildingDataAccessObject extends DBDataAccessObject {

    static MongoCollection<Document> collection;
    static final List<String> allowedAttributes = List.of(new String[]{
            "buildingCode", "shortName", "longName", "location", "controlInfo"});

    public DBBuildingDataAccessObject() {
        super();    // initializes the MongoClient and MongoDatabase from
        // the set URI
        collection = database.getCollection("Buildings");
    }

    public DBBuildingDataAccessObject(MongoDatabase database) {
        super(database);
        collection = database.getCollection("Buildings");
    }

    /**
     * Returns all buildings who satisfy all the given conditions
     *
     * @param conditions a list of condition objects that the returned buildings must satisfy
     * @return The buildings that match all the conditions
     */
    public static List<entity.Building> getMatching(Iterable<AbstractCondition<?>> conditions) {
        return new ArrayList<>(getMatchingIDMap(conditions).values());

    }

    /**
     * Returns all buildings who satisfy the condition
     *
     * @param condition a condition object that the returned buildings must satisfy
     * @return The buildings that match the conditions
     */
    public static List<entity.Building> getMatching(AbstractCondition<?> condition) {

        List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        return getMatching(conditions);

    }

    /**
     * Returns all buildings which satisfy all the given conditions, with database IDs
     *
     * @param conditions a list of condition objects that the returned buildings must satisfy
     * @return The buildings that match all the conditions mapped to their IDs in the database.
     */
    public static Map<String, entity.Building> getMatchingIDMap(Iterable<AbstractCondition<?>> conditions) {

        checkAttribute(conditions);

        Bson filter = parseConditions(conditions);
        List<Document> docs = getAll(filter);

        Map<String, entity.Building> buildings = new HashMap<>();
        for (Document doc : docs) {
            entity.Building building = createBuilding(doc);
            buildings.put(MongoDocuments.id(doc), building);
        }
        return buildings;

    }

    /**
     * Returns all buildings which satisfy the given condition, with database IDs
     *
     * @param condition a condition object that the returned buildings must satisfy
     * @return The buildings that match the condition mapped to their IDs in the database.
     */
    public static Map<String, entity.Building> getMatchingIDMap(AbstractCondition<?> condition) {

        List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        return getMatchingIDMap(conditions);

    }

    /**
     * Parses a list of AbstractCondition objects into a single Bson filter
     *
     * @param conditions list of condition objects to be connected by and statements
     * @return a Bson filter representing satisfying all conditions
     */
    private static Bson parseConditions(Iterable<AbstractCondition<?>> conditions) {
        List<Bson> filters = new ArrayList<>();
        conditions.forEach((condition) -> filters.add(condition.getFilter()));
        return filters.isEmpty() ? new Document() : Filters.and(filters);
    }

    /**
     * Return a list of Documents which match the specified parameters
     *
     * @param filter the filter that must be satisfied for the Document to be returned
     * @return The list of valid documents
     */
    private static <T> List<Document> getAll(Bson filter) {
        List<Document> docs = new ArrayList<>();
        return collection.find(filter).into(docs);
    }

    /**
     * Creates a Building object out of the inputted Document
     *
     * @param doc Document containing building data for a specific building
     * @return the Building object constructed using that data
     */
    private static entity.Building createBuilding(Document doc) {
        return new entity.Building(
                MongoDocuments.string(doc, MongoDocuments.id(doc), "buildingCode", "code"),
                MongoDocuments.string(doc, "Unknown building", "longName", "shortName", "name"),
                getLatitude(doc), getLongitude(doc));
    }

    /**
     * Writes a single Building object to the database. Latitude and Longitude are converted
     * to a location object for geospatial filtering
     *
     * @param building The Building object to be written.
     * @return the ID for the written object.
     */
    public String write(Building building) {
        Document doc = new Document();
        doc.append("buildingCode", building.getBuildingCode());
        doc.append("shortName", building.getBuildingNameShort());
        doc.append("longName", building.getBuildingNameLong());
        doc.append("location", createLocation(building));
        doc.append("controlInfo", building.getControlInfo());

        return collection.insertOne(doc).getInsertedId().toString();
    }

    /**
     * Parse Latitude and Longitude of a Building into a Document
     *
     * @param building Building object with longitude and latitude
     * @return Document that can be inserted into the database and used for geospatial filters
     */
    private static Document createLocation(Building building) {
        Document location = new Document("type", "Point");
        List<Double> coords = new ArrayList<>();
        coords.add(0, building.getLongitude());
        coords.add(1, building.getLatitude());
        location.append("coordinates", coords);
        return location;
    }

    /**
     * Return the longitude as a double parsed from a Document containing
     * geospatial data
     *
     * @param doc Document of valid format:
     *            type: "Point"
     *            coordinates: [longitude, latitude]
     * @return Double representing longitude
     */
    private static double getLongitude(Document doc) {
        Document location = doc.get("location", Document.class);
        List<?> coordinates = location == null ? List.of() : location.getList("coordinates", Object.class, List.of());
        return coordinates.size() > 0 && coordinates.get(0) instanceof Number coordinate
                ? coordinate.doubleValue() : MongoDocuments.number(doc, 0, "longitude", "lng");
    }

    /**
     * Return the latitude as a double parsed from a Document containing
     * geospatial data
     *
     * @param doc Document of valid format:
     *            {type: "Point"
     *            coordinates: [longitude, latitude]}
     * @return Double representing latitude
     */
    private static double getLatitude(Document doc) {
        Document location = doc.get("location", Document.class);
        List<?> coordinates = location == null ? List.of() : location.getList("coordinates", Object.class, List.of());
        return coordinates.size() > 1 && coordinates.get(1) instanceof Number coordinate
                ? coordinate.doubleValue() : MongoDocuments.number(doc, 0, "latitude", "lat");
    }

    /**
     * Checks the condition against the list of allowed attributes, throwing a
     * runtime exception if it's not a valid attribute.
     *
     * @param condition The condition to check.
     */
    private static void checkAttribute(AbstractCondition<?> condition) {
        if (!allowedAttributes.contains(condition.getFieldName())) {
            throw new RuntimeException("Not a valid attribute");
        }
    }

    /**
     * Checks the conditions against the list of allowed attributes, throwing a
     * runtime exception if it's not a valid attribute.
     *
     * @param conditions The conditions to check.
     */
    private static void checkAttribute(Iterable<AbstractCondition<?>> conditions) {
        for (AbstractCondition<?> condition : conditions) {
            checkAttribute(condition);
        }
    }

    /**
     * Deletes every entry in the database that matches the given conditions
     *
     * @param conditions List of AbstractCondition objects. An object must satisfy
     *                   all conditions to be deleted
     */
    public void delete(Iterable<AbstractCondition<?>> conditions) {
        checkAttribute(conditions);

        Bson filter = parseConditions(conditions);
        collection.deleteMany(filter);
    }

    /**
     * Deletes every entry in the database that matches the given condition
     *
     * @param condition A AbstractCondition object that the object must satisfy.
     */
    public void delete(AbstractCondition<?> condition) {
        List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        delete(conditions);
    }

    /**
     * Production campus-location seed used by the current Swing application.
     */
    public List<entity.Building> ensureLocations(List<entity.Building> locations) {
        List<entity.Building> persisted = new ArrayList<>();
        for (entity.Building location : locations) {
            List<entity.Building> existing = getMatching(new Condition<>("buildingCode", Operator.EQ, location.code()));
            if (existing.isEmpty()) write(location);
            Document geoPoint = new Document("type", "Point")
                    .append("coordinates", List.of(location.longitude(), location.latitude()));
            collection.updateOne(Filters.eq("buildingCode", location.code()),
                    Updates.combine(
                            Updates.set("longName", location.name()),
                            Updates.set("location", geoPoint),
                            Updates.setOnInsert("shortName", location.name()),
                            Updates.setOnInsert("controlInfo", "U of T St. George campus reference location")
                    ), new UpdateOptions().upsert(true));
            List<entity.Building> refreshed = getMatching(new Condition<>("buildingCode", Operator.EQ, location.code()));
            if (!refreshed.isEmpty()) persisted.add((entity.Building) refreshed.getFirst());
        }
        return List.copyOf(persisted);
    }

    public static List<Building> loadBuildings(String filename) throws Exception {
        List<Building> buildings = new ArrayList<>();

        try (Reader reader = new FileReader(filename);
             JsonReader jsonReader = Json.createReader(reader)) {

            JsonArray jsonArray = jsonReader.readArray();

            for (JsonValue value : jsonArray) {
                JsonObject obj = (JsonObject) value;

                String id = obj.getString("ID");
                String name = obj.getString("name");

                // Stored as strings in the JSON
                double latitude = Double.parseDouble(obj.getString("latitude"));
                double longitude = Double.parseDouble(obj.getString("longitude"));

                buildings.add(new entity.Building(id, name, latitude, longitude));
            }
        }

        return buildings;
    }

    public static void main(String[] args) throws FileNotFoundException {
        /**
        DBBuildingDataAccessObject buildingDAO = new DBBuildingDataAccessObject();
        Condition condition = new Condition<>("buildingCode", Operator.NE, "00");
        buildingDAO.delete(condition);
        try {
            List<Building> buildingList = loadBuildings("src/main/resources/data/building.json");
            for (Building building : buildingList) {
                buildingDAO.write(building);
            }
            System.out.println("Success!");
        } catch (Exception e) {
            System.out.println("Building entrance failed. ");
        }
         */
        DBBuildingDataAccessObject buildingDAO = new DBBuildingDataAccessObject();
        Condition condition = new Condition<>("buildingCode", Operator.NE, "00");
        System.out.println(buildingDAO.getMatching(condition));


    }
}
