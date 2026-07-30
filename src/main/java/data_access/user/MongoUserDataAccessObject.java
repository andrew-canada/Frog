package data_access.user;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import entity.User;
import org.bson.Document;
import use_case.gateway.UserDataAccessInterface;
import java.util.Optional;

public final class MongoUserDataAccessObject implements UserDataAccessInterface {
    private final MongoCollection<Document> users;
    private User currentUser;

    public MongoUserDataAccessObject(MongoDatabase database) { users = database.getCollection("Users"); }

    @Override public Optional<User> get(String username) {
        Document document = users.find(Filters.or(Filters.eq("username", username), Filters.eq("name", username))).first();
        if (document == null) return Optional.empty();
        String storedName = value(document, username, "username", "name");
        String passwordHash = value(document, "", "passwordHash", "password");
        if (passwordHash.isBlank()) return Optional.empty();
        return Optional.of(new User(storedName, passwordHash));
    }

    @Override public boolean existsByName(String username) { return get(username).isPresent(); }

    @Override public void save(User user) {
        Document document = new Document("username", user.username()).append("passwordHash", user.passwordHash());
        users.replaceOne(Filters.or(Filters.eq("username", user.username()), Filters.eq("name", user.username())),
                document, new ReplaceOptions().upsert(true));
    }

    @Override public void setCurrentUser(User user) { currentUser = user; }
    @Override public Optional<User> getCurrentUser() { return Optional.ofNullable(currentUser); }

    private static String value(Document document, String fallback, String... fields) {
        for (String field : fields) {
            String value = document.getString(field);
            if (value != null && !value.isBlank()) return value;
        }
        return fallback;
    }
}

