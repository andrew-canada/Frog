package data_access.washroom;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import data_access.Condition;
import data_access.MongoDocuments;
import data_access.Operator;
import data_access.building.DBBuildingDataAccessObject;
import entity.building.Building;
import entity.building.GenericBuildingFactory;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import entity.washroom.Washroom;
import entity.washroom.GenericWashroomFactory;
import data_access.DBDataAccessObject;

import javax.json.*;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;

public class DBWashroomDataAccessObject extends DBDataAccessObject implements use_case.gateway.WashroomDataAccessInterface {

    static MongoCollection<Document> collection;
    private static MongoCollection<Document> buildings;
    private static MongoCollection<Document> reviews;
    static final List<String> allowedAttributes = List.of(new String[] {
            "buildingID", "floor"});

    public DBWashroomDataAccessObject() {
        super();    // initializes the MongoClient and MongoDatabase from
        // the set URI
        collection = database.getCollection("Washrooms");
        buildings = database.getCollection("Buildings");
        reviews = database.getCollection("Reviews");
    }

    public DBWashroomDataAccessObject(MongoDatabase database) {
        super(database);
        collection = database.getCollection("Washrooms");
        buildings = database.getCollection("Buildings");
        reviews = database.getCollection("Reviews");
    }

    /**
     * Returns all washrooms who satisfy all the given conditions
     * @param conditions a list of condition objects that the returned washrooms must satisfy
     * @return The washrooms that match all the conditions
     */
    public static List<Washroom> getMatching(Iterable<Condition<?>> conditions) {
        return new ArrayList<>(getMatchingIDMap(conditions).values());

    }

    /**
     * Returns all buildings who satisfy the condition
     * @param condition a condition object that the returned buildings must satisfy
     * @return The buildings that match the conditions
     */
    public static List<Washroom> getMatching(Condition<?> condition) {

        List<Condition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        return getMatching(conditions);

    }

    /**
     * Returns all washrooms which satisfy all the given conditions, with database IDs
     * @param conditions a list of condition objects that the returned washrooms must satisfy
     * @return The washrooms that match all the conditions mapped to their IDs in the database.
     */
    public static Map<String, Washroom> getMatchingIDMap(Iterable<Condition<?>> conditions) {
        checkAttribute(conditions);

        Bson filter = parseConditions(conditions);
        List<Document> docs = getAll(filter);

        Map<String, Washroom> washrooms = new HashMap<>();
        for (Document doc : docs) {
            Washroom washroom = createWashroom(doc);
            washrooms.put(MongoDocuments.id(doc), washroom);
        }
        return washrooms;
    }

    /**
     * Returns all buildings which satisfy the given condition, with database IDs
     * @param condition a condition object that the returned buildings must satisfy
     * @return The buildings that match the condition mapped to their IDs in the database.
     */
    public static Map<String, Washroom> getMatchingIDMap(Condition<?> condition) {

        List<Condition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        return getMatchingIDMap(conditions);

    }

    /**
     * Parses a list of Condition objects into a single Bson filter
     * @param conditions list of condition objects to be connected by and statements
     * @return a Bson filter representing satisfying all conditions
     */
    private static Bson parseConditions(Iterable<Condition<?>> conditions) {
        List<Bson> filters = new ArrayList<>();
        conditions.forEach((condition) -> filters.add(condition.getFilter()));
        return filters.isEmpty() ? new Document() : Filters.and(filters);
    }

    /**
     * Return a list of Documents which match the specified parameters
     * @param filter the filter that must be satisfied for the Document to be returned
     * @return The list of valid documents
     */
    private static <T> List<Document> getAll(Bson filter) {
        List<Document> docs = new ArrayList<>();
        return collection.find(filter).into(docs);
    }

    /**
     * Creates a washroom object out of the inputted Document
     * @param doc Document containing washroom data for a specific washroom
     * @return the washroom object constructed using that data
     */
    private static Washroom createWashroom(Document doc) {
        String id = MongoDocuments.id(doc);
        String buildingId = MongoDocuments.string(doc, "", "buildingID", "buildingId");
        Document buildingDocument = MongoDocuments.findById(buildings, buildingId);
        if (buildingDocument == null) {
            String buildingCode = MongoDocuments.string(doc, buildingId, "buildingCode");
            buildingDocument = buildings.find(new Document("buildingCode", buildingCode)).first();
        }
        if (buildingDocument == null) throw new IllegalStateException("Washroom " + id + " references a missing building.");
        entity.Building building = toApplicationBuilding(buildingDocument);
        String floor = MongoDocuments.string(doc, "Unknown floor", "floor");
        String name = MongoDocuments.string(doc, building.name() + ", " + floor, "name");
        entity.Washroom.Gender gender = parseGender(MongoDocuments.string(doc, "ALL_GENDER", "gender"));
        return new entity.Washroom(id, name, building, floor,
                MongoDocuments.bool(doc, false, "accessible"), gender,
                Math.max(0, MongoDocuments.integer(doc, 0, "numToilets", "toilets")),
                Math.max(0, MongoDocuments.integer(doc, 0, "numSinks", "sinks")),
                MongoDocuments.string(doc, "Inside " + building.name(), "locationDescription", "description"),
                summary(id));
    }

    /**
     * Checks the conditions against the list of allowed attributes, throwing a
     * runtime exception if it's not a valid attribute.
     * @param conditions The conditions to check.
     */
    private static void checkAttribute(Iterable<Condition<?>> conditions) {
        for (Condition<?> condition: conditions) {
            checkAttribute(condition);
        }
    }

    /**
     * Checks the condition against the list of allowed attributes, throwing a
     * runtime exception if it's not a valid attribute.
     * @param condition The condition to check.
     */
    private static void checkAttribute(Condition<?> condition) {
        if (!allowedAttributes.contains(condition.getFieldName())) {
            throw new RuntimeException("Not a valid attribute");
        }
    }

    /**
     * Writes a single Washroom object to the database.
     * @param washroom The Washroom object to be written.
     */
    public static String write(Washroom washroom, String buildingID) {
        if (washroom instanceof entity.Washroom applicationWashroom) {
            Document doc = new Document();
            doc.append("buildingID", buildingID);
            doc.append("buildingCode", applicationWashroom.building().code());
            doc.append("seedKey", "campus-" + applicationWashroom.building().code().toLowerCase(Locale.ROOT) + "-washroom");
            doc.append("name", applicationWashroom.name());
            doc.append("floor", applicationWashroom.floor());
            doc.append("gender", applicationWashroom.gender().name());
            doc.append("accessible", applicationWashroom.accessible());
            doc.append("numToilets", applicationWashroom.numToilets());
            doc.append("numSinks", applicationWashroom.numSinks());
            doc.append("locationDescription", applicationWashroom.locationDescription());
            return collection.insertOne(doc).getInsertedId().toString();
        }
        Document doc = new Document();
        doc.append("buildingID", buildingID);
        doc.append("floor", washroom.getFloor());

        return collection.insertOne(doc).getInsertedId().toString();
    }

    /**
     * Deletes every entry in the database that matches the given conditions
     * @param conditions List of Condition objects. An object must satisfy
     *                   all conditions to be deleted
     */
    public static void delete(Iterable<Condition<?>> conditions) {
        checkAttribute(conditions);
        Bson filter = parseConditions(conditions);
        collection.deleteMany(filter);
    }

    /**
     * Deletes every entry in the database that matches the given condition
     * @param condition A Condition object that the object must satisfy.
     */
    public void delete(Condition<?> condition) {
        List<Condition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        delete(conditions);

    }

    /** Idempotently creates one selectable washroom per campus landmark. */
    public void ensureCampusWashrooms(List<entity.Building> campusBuildings) {
        for (entity.Building building : campusBuildings) {
            Document buildingDocument = buildings.find(Filters.eq("buildingCode", building.code())).first();
            if (buildingDocument == null) continue;
            Object buildingId = buildingDocument.get("_id");
            String seedKey = "campus-" + building.code().toLowerCase(Locale.ROOT) + "-washroom";
            Document existing = collection.find(Filters.eq("seedKey", seedKey)).first();
            entity.Washroom campusWashroom = new entity.Washroom(seedKey, building.name(), building, "Main floor", false,
                    entity.Washroom.Gender.ALL_GENDER, 0, 0, "Main-floor washroom location at " + building.name(),
                    entity.ReviewSummary.empty());
            if (existing == null) write(campusWashroom, buildingId.toString());
            else collection.updateOne(Filters.eq("seedKey", seedKey),
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
                    ));
        }
    }

    @Override public Optional<entity.Washroom> getById(String id) {
        Document document = MongoDocuments.findById(collection, id);
        return document == null ? Optional.empty() : Optional.of((entity.Washroom) createWashroom(document));
    }

    @Override public List<entity.Washroom> getAll() {
        return getMatching(List.<Condition<?>>of()).stream().map(washroom -> (entity.Washroom) washroom).toList();
    }

    @Override public List<entity.Washroom> getNearby(double latitude, double longitude, double radiusMeters) {
        return getAll().stream().filter(washroom -> distance(latitude, longitude,
                washroom.building().latitude(), washroom.building().longitude()) <= radiusMeters).toList();
    }

    private static entity.ReviewSummary summary(String washroomId) {
        double rating = 0, cleanliness = 0; int count = 0;
        for (Document review : reviews.find()) {
            if (!MongoDocuments.referenceMatches(review.get("washroomID"), washroomId)
                    && !MongoDocuments.referenceMatches(review.get("washroomId"), washroomId)) continue;
            rating += clampRating(MongoDocuments.number(review, 0, "rating", "stars"));
            cleanliness += clampRating(MongoDocuments.number(review,
                    MongoDocuments.number(review, 0, "rating", "stars"), "cleanliness"));
            count++;
        }
        return count == 0 ? entity.ReviewSummary.empty() : new entity.ReviewSummary(rating / count, cleanliness / count, count);
    }

    private static entity.Building toApplicationBuilding(Document document) {
        Document location = document.get("location", Document.class);
        List<?> coordinates = location == null ? List.of() : location.getList("coordinates", Object.class, List.of());
        double longitude = coordinates.size() > 0 && coordinates.get(0) instanceof Number number
                ? number.doubleValue() : MongoDocuments.number(document, 0, "longitude", "lng");
        double latitude = coordinates.size() > 1 && coordinates.get(1) instanceof Number number
                ? number.doubleValue() : MongoDocuments.number(document, 0, "latitude", "lat");
        String code = MongoDocuments.string(document, MongoDocuments.id(document), "buildingCode", "code");
        String name = MongoDocuments.string(document, code, "longName", "shortName", "name");
        return new entity.Building(code, name, latitude, longitude);
    }

    private static entity.Washroom.Gender parseGender(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        try { return entity.Washroom.Gender.valueOf(normalized); }
        catch (IllegalArgumentException ignored) { return entity.Washroom.Gender.ALL_GENDER; }
    }

    private static double clampRating(double value) { return Math.max(1, Math.min(5, value)); }
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double y = Math.toRadians(lat2 - lat1);
        return Math.sqrt(x * x + y * y) * 6_371_000;
    }

    public static List<Washroom> loadWashrooms(String filename) throws Exception {
        List<Washroom> washroomList = new ArrayList<>();
        try (Reader reader = new FileReader(filename);
             JsonReader jsonReader = Json.createReader(reader)) {

            JsonArray jsonArray = jsonReader.readArray();

            DBBuildingDataAccessObject dbBuildingDataAccessObject = new DBBuildingDataAccessObject();

            for (JsonValue value : jsonArray) {
                JsonObject obj = (JsonObject) value;

                String id = obj.getString("ID");
                String name = obj.getString("name");
                String gender = obj.getString("gender");
                Condition condition = new Condition("BuildingCode", Operator.EQ, id);
                if(!dbBuildingDataAccessObject.getMatching(condition).isEmpty()){
                    washroomList.add(new entity.Washroom(id,
                            name,
                            dbBuildingDataAccessObject.getMatching(condition).get(0)),

                    );

                }
            }
        }

        return washroomList;
    }

    public static void main(String[] args) {
        /*
        Washroom washroom = GenericWashroomFactory.create("1");
        DBWashroomDataAccessObject accessor = new DBWashroomDataAccessObject();
        DBBuildingDataAccessObject baccessor = new DBBuildingDataAccessObject();

        accessor.write(washroom,
                (String) baccessor.getMatchingIDMap(
                        new Condition<String>("buildingCode", Operator.EQ, "TEST")).keySet().toArray()[0]);
                        */

    }
}
