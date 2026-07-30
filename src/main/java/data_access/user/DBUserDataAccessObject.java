package data_access.user;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import data_access.Condition;
import data_access.MongoDocuments;
import entity.user.LoggedInUser;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import entity.user.User;
import entity.user.LoggedInUserFactory;
import data_access.DBDataAccessObject;

import java.util.*;

public class DBUserDataAccessObject extends DBDataAccessObject implements use_case.gateway.UserDataAccessInterface {

    static MongoCollection<Document> collection;
    private entity.User currentApplicationUser;

    public DBUserDataAccessObject() {
        super();    // initializes the MongoClient and MongoDatabase from
        // the set URI
        collection = database.getCollection("Users");
    }

    public DBUserDataAccessObject(MongoDatabase database) {
        super(database);
        collection = database.getCollection("Users");
    }

    /**
     * Returns all users who satisfy all the given conditions
     * @param conditions a list of condition objects that the returned users must satisfy
     * @return The users that match all the conditions
     */
    public List<LoggedInUser> getMatching(Iterable<Condition<?>> conditions) {
        return new ArrayList<>(getMatchingIDMap(conditions).values());
    }

    /**
     * Returns all users which satisfy all the given conditions, with database IDs
     * @param conditions a list of condition objects that the returned users must satisfy
     * @return The users that match all the conditions mapped to their IDs in the database.
     */
    public Map<String, LoggedInUser> getMatchingIDMap(Iterable<Condition<?>> conditions) {

        Bson filter = parseConditions(conditions);
        List<Document> docs = getAll(filter);

        Map<String, LoggedInUser> users = new HashMap<>();
        for (Document doc : docs) {
            LoggedInUser user = createUser(doc);
            users.put(MongoDocuments.id(doc), user);
        }
        return users;
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
     * Creates a user object out of the inputted Document
     * @param doc Document containing user data for a specific user
     * @return the user object constructed using that data
     */
    private static LoggedInUser createUser(Document doc) {
        return new LoggedInUser(value(doc, "unknown", "username", "name"),
                value(doc, "", "passwordHash", "password"), List.of(),
                value(doc, "", "personalPlan"));
    }

    /**
     * Writes a single LoggedInUser object to the database.
     * @param user The LoggedInUser object to be written.
     */
    public String write(LoggedInUser user, String washroomID) {
        Document doc = new Document("username", user.getName()).append("passwordHash", user.getPassword())
                .append("personalPlan", user.getPersonalPlan());
        if (washroomID != null && !washroomID.isBlank()) doc.append("washroomID", washroomID);
        collection.replaceOne(Filters.or(Filters.eq("username", user.getName()), Filters.eq("name", user.getName())),
                doc, new ReplaceOptions().upsert(true));
        Document persisted = collection.find(Filters.eq("username", user.getName())).first();
        return persisted == null ? user.getName() : MongoDocuments.id(persisted);
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

    @Override public Optional<entity.User> get(String username) {
        List<LoggedInUser> matches = getMatching(List.of(new Condition<>("username", data_access.Operator.EQ, username)));
        if (matches.isEmpty()) matches = getMatching(List.of(new Condition<>("name", data_access.Operator.EQ, username)));
        if (matches.isEmpty() || matches.getFirst().getPassword().isBlank()) return Optional.empty();
        LoggedInUser user = matches.getFirst();
        return Optional.of(new entity.User(user.getName(), user.getPassword(), user.getPersonalPlan()));
    }

    @Override public boolean existsByName(String username) { return get(username).isPresent(); }

    @Override public void save(entity.User user) {
        write(new LoggedInUser(user.username(), user.passwordHash(), List.of(), user.personalPlan()), null);
    }

    @Override public void setCurrentUser(entity.User user) { currentApplicationUser = user; }
    @Override public Optional<entity.User> getCurrentUser() { return Optional.ofNullable(currentApplicationUser); }
    @Override public void removeUser(String username) {
        delete(List.of(new Condition<>("username", data_access.Operator.EQ, username)));
        delete(List.of(new Condition<>("name", data_access.Operator.EQ, username)));
    }

    private static String value(Document document, String fallback, String... fields) {
        for (String field : fields) {
            String value = document.getString(field);
            if (value != null && !value.isBlank()) return value;
        }
        return fallback;
    }
}
