package database.user;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import database.AbstractCondition;
import database.Condition;
import database.DBDataAccessObject;
import database.MongoDocuments;
import database.Operator;
import entity.User;
import use_case.moderate_reviews.ModeratorDataAccessInterface;
import use_case.port.CurrentUserSession;
import use_case.port.UserRepository;

public class DBUserDataAccessObject extends DBDataAccessObject
    implements UserRepository, CurrentUserSession, ModeratorDataAccessInterface {

    private static final List<String> allowedAttributes = List.of(new String[] {"username", "passwordHash", "personalPlan"});
    private final MongoCollection<Document> collection;
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private entity.User currentApplicationUser;

    public DBUserDataAccessObject() {
        // initializes the MongoClient and MongoDatabase from
        super();
       // the set URI

        collection = database.getCollection("Users");
    }

    public DBUserDataAccessObject(final MongoDatabase database) {
        super(database);
        collection = database.getCollection("Users");
    }

    /**
     * Parses a list of AbstractCondition objects into a single Bson filter
     *
     * @param conditions list of condition objects to be connected by and statements
     * @return a Bson filter representing satisfying all conditions
     */
    private static Bson parseConditions(final Iterable<AbstractCondition<?>> conditions) {
        final List<Bson> filters = new ArrayList<>();
        conditions.forEach((condition) -> {
            filters.add(condition.getFilter());
        });
        if (filters.isEmpty()) {
            return new Document();
        }
        return Filters.and(filters);
    }

    /**
     * Return a list of Documents which match the specified parameters
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
     * Creates a user object out of the inputted Document
     *
     * @param doc Document containing user data for a specific user
     * @return the user object constructed using that data
     */
    private static User createUser(final Document doc) {
        return new User(value(doc, "unknown", "username", "name"), value(doc, "", "passwordHash", "password"),
            value(doc, "", "personalPlan"), doc.getBoolean("isModerator", false));
    }

    /**
     * Checks the condition against the list of allowed attributes, throwing a
     * runtime exception if it's not a valid attribute.
     *
     * @param condition The condition to check.
     */
    private static void checkAttribute(final AbstractCondition<?> condition) {
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
    private static void checkAttribute(final Iterable<AbstractCondition<?>> conditions) {
        for (final AbstractCondition<?> condition : conditions) {
            checkAttribute(condition);
        }
    }

    private static String value(final Document document, final String fallback, final String... fields) {
        for (final String field : fields) {
            final String value = document.getString(field);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return fallback;
    }

    public void addPropertyChangeListener(final PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    /**
     * Returns all users who satisfy all the given conditions
     *
     * @param conditions a list of condition objects that the returned users must satisfy
     * @return The users that match all the conditions
     */
    private List<User> getMatching(final Iterable<AbstractCondition<?>> conditions) {
        return new ArrayList<>(getMatchingIDMap(conditions).values());
    }

    /**
     * Returns all users which satisfy all the given conditions, with database IDs
     *
     * @param conditions a list of condition objects that the returned users must satisfy
     * @return The users that match all the conditions mapped to their IDs in the database.
     */
    private Map<String, User> getMatchingIDMap(final Iterable<AbstractCondition<?>> conditions) {
        checkAttribute(conditions);

        final Bson filter = parseConditions(conditions);
        final List<Document> docs = getAll(filter);

        final Map<String, User> users = new HashMap<>();
        for (final Document doc : docs) {
            final User user = createUser(doc);
            users.put(MongoDocuments.id(doc), user);
        }
        return users;
    }

    /**
     * Writes a single application user to the database.
     *
     * @param user The user object to be written.
     */
    private String write(final User user, final String washroomID) {
        final Document doc = new Document("username", user.username())
            .append("passwordHash", user.passwordHash())
            .append("personalPlan", user.personalPlan())
            .append("isModerator", user.isModerator());
        if (washroomID != null && !washroomID.isBlank()) {
            doc.append("washroomID", washroomID);
        }
        collection.replaceOne(Filters.or(Filters.eq("username", user.username()), Filters.eq("name", user.username())),
            doc, new ReplaceOptions().upsert(true));
        final Document persisted = collection
            .find(Filters.eq("username", user.username()))
            .first();
        if (persisted == null) {
            return user.username();
        }
        return MongoDocuments.id(persisted);
    }

    /**
     * Deletes every entry in the database that matches the given conditions
     *
     * @param conditions List of AbstractCondition objects. An object must satisfy
     *                   all conditions to be deleted
     */
    private void delete(final Iterable<AbstractCondition<?>> conditions) {
        checkAttribute(conditions);
        final Bson filter = parseConditions(conditions);
        collection.deleteMany(filter);
    }

    @Override
    public Optional<entity.User> get(final String username) {
        List<User> matches = getMatching(List.of(new Condition<>("username", Operator.EQ, username)));
        if (matches.isEmpty()) {
            matches = getMatching(List.of(new Condition<>("username", Operator.EQ, username)));
        }
        if (matches.isEmpty() || matches
            .getFirst()
            .passwordHash()
            .isBlank()) {
            return Optional.empty();
        }
        return Optional.of(matches.getFirst());
    }

    @Override
    public boolean existsByName(final String username) {
        return get(username).isPresent();
    }

    @Override
    public void save(final entity.User user) {
        write(user, null);
    }

    @Override
    public boolean isModerator(final String username) {
        return get(username)
            .map(entity.User::isModerator)
            .orElse(false);
    }

    /**
     * Grants moderator to an existing account. No-op if the account doesn't exist.
     *
     * @param username the account to promote to moderator
     */
    public void ensureModerator(final String username) {
        collection.updateOne(Filters.eq("username", username), new Document("$set", new Document("isModerator", true)));
    }

    @Override
    public Optional<entity.User> currentUser() {
        return Optional.ofNullable(currentApplicationUser);
    }

    @Override
    public void setCurrentUser(final entity.User user) {
        final entity.User prev = currentApplicationUser;
        currentApplicationUser = user;
        if (Objects.isNull(prev) || Objects.isNull(user)) {
            changes.firePropertyChange("state", prev, user);
        }
    }

    @Override
    public void clear() {
        setCurrentUser(null);
    }

    @Override
    public void removeUser(final String username) {
        delete(List.of(new Condition<>("username", Operator.EQ, username)));
    }
}
