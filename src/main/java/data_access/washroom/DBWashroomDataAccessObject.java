package data_access.washroom;

import com.mongodb.client.model.Filters;
import data_access.Condition;
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

import java.util.*;

public class DBWashroomDataAccessObject extends DBDataAccessObject {

    static MongoCollection<Document> collection;
    static final List<String> allowedAttributes = List.of(new String[] {
            "buildingID", "floor"});

    public DBWashroomDataAccessObject() {
        super();    // initializes the MongoClient and MongoDatabase from
        // the set URI
        collection = database.getCollection("Washrooms");
    }

    /**
     * Returns all washrooms who satisfy all the given conditions
     * @param conditions a list of condition objects that the returned washrooms must satisfy
     * @return The washrooms that match all the conditions
     */
    public static List<Washroom> getMatching(Iterable<Condition<?>> conditions) {

        Bson filter = parseConditions(conditions);
        List<Document> docs = getAll(filter);

        List<Washroom> washrooms = new ArrayList<>();
        for (Document doc: docs) {
            Washroom washroom = createWashroom(doc);
            washrooms.add(washroom);
        }
        return washrooms;

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

        Bson filter = parseConditions(conditions);
        List<Document> docs = getAll(filter);

        Map<String, Washroom> washrooms = new HashMap<>();
        for (Document doc : docs) {
            Washroom washroom = createWashroom(doc);
            washrooms.put(doc.getString("_id"), washroom);
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
        Bson finalFilter;
        List<Bson> filters = new ArrayList<>();
        conditions.forEach((condition) -> filters.add(condition.getFilter()));
        finalFilter = Filters.and(filters);
        return finalFilter;
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
        Washroom washroom = GenericWashroomFactory.create(
                doc.getString("floor"));
        return washroom;
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

    public static void main(String[] args) {
        Washroom washroom = GenericWashroomFactory.create("1");
        DBWashroomDataAccessObject accessor = new DBWashroomDataAccessObject();
        DBBuildingDataAccessObject baccessor = new DBBuildingDataAccessObject();

        accessor.write(washroom,
                (String) baccessor.getMatchingIDMap(
                        new Condition<String>("buildingCode", Operator.EQ, "TEST")).keySet().toArray()[0]);
    }
}
