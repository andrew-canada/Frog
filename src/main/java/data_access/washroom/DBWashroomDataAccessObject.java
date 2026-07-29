package data_access.washroom;

import com.mongodb.client.model.Filters;
import data_access.Condition;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import entity.washroom.Washroom;
import entity.washroom.GenericWashroomFactory;
import data_access.DBDataAccessObject;

import java.util.*;

public class DBWashroomDataAccessObject extends DBDataAccessObject {

    static MongoCollection<Document> collection;

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
    private static Washroom createUser(Document doc) {
        Washroom washroom = GenericWashroomFactory.create(
                doc.getString("floor");
        return washroom;
    }

    /**
     * Writes a single Washroom object to the database.
     * @param washroom The Washroom object to be written.
     */
    public String write(Washroom washroom, String buildingID) {
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
    public void delete(Iterable<Condition<?>> conditions) {
        Bson filter = parseConditions(conditions);
        collection.deleteMany(filter);
    }
}
