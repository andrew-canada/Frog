package data_access.user;

import com.mongodb.client.model.Filters;
import data_access.Condition;
import entity.building.Building;
import entity.user.LoggedInUser;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import entity.user.User;
import entity.user.LoggedInUserFactory;
import data_access.DBDataAccessObject;

import java.util.*;

public class DBUserDataAccessObject extends DBDataAccessObject {

    static MongoCollection<Document> collection;

    public DBReviewDataAccessObject() {
        super();    // initializes the MongoClient and MongoDatabase from
        // the set URI
        collection = database.getCollection("Users");
    }

    /**
     * Returns all users who satisfy all the given conditions
     * @param conditions a list of condition objects that the returned users must satisfy
     * @return The users that match all the conditions
     */
    public static List<LoggedInUser> getMatching(Iterable<Condition<?>> conditions) {

        Bson filter = parseConditions(conditions);
        List<Document> docs = getAll(filter);

        List<LoggedInUser> users = new ArrayList<>();
        for (Document doc: docs) {
            LoggedInUser user = createUser(doc);
            users.add(user);
        }
        return users;

    }

    /**
     * Returns all users which satisfy all the given conditions, with database IDs
     * @param conditions a list of condition objects that the returned users must satisfy
     * @return The users that match all the conditions mapped to their IDs in the database.
     */
    public static Map<String, LoggedInUser> getMatchingIDMap(Iterable<Condition<?>> conditions) {

        Bson filter = parseConditions(conditions);
        List<Document> docs = getAll(filter);

        Map<String, LoggedInUser> users = new HashMap<>();
        for (Document doc : docs) {
            LoggedInUser user = createUser(doc);
            users.put(doc.getString("_id"), user);
        }
        return users;
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
     * Creates a user object out of the inputted Document
     * @param doc Document containing user data for a specific user
     * @return the user object constructed using that data
     */
    private static LoggedInUser createUser(Document doc) {
        User user = LoggedInUserFactory.create(
                doc.getString("name"),
                doc.getString("password"));
        return user;
    }

    /**
     * Writes a single LoggedInUser object to the database.
     * @param user The LoggedInUser object to be written.
     */
    public String write(LoggedInUser user, String washroomID) {
        Document doc = new Document();
        doc.append("name", user.getName());
        doc.append("password", user.getPassword());

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
