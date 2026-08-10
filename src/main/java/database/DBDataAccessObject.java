package database;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Owns the application's single MongoDB client and exposes its configured database.
 */
public class DBDataAccessObject implements AutoCloseable {
    private static final String URI_ENV = "MONGODB_URI";
    private static final String DATABASE_ENV = "MONGODB_DATABASE";

    private final MongoClient client;
    private final boolean ownsClient;
    private final MongoDatabase database;

    /**
     * Preserves the team DAO constructors while moving credentials to environment variables.
     */
    public DBDataAccessObject() {
        this(requiredUri(), configuredDatabaseName());
    }

    private DBDataAccessObject(final String uri, final String databaseName) {
        client = MongoClients.create(uri);
        database = client.getDatabase(databaseName);
        ownsClient = true;
    }

    /**
     * Allows additive adapters/subclasses to share a client owned by the composition root.
     * @param database parameter value.
     */
    protected DBDataAccessObject(final MongoDatabase database) {
        this.client = null;
        this.database = database;
        this.ownsClient = false;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public static DBDataAccessObject fromEnvironment() {
        return new DBDataAccessObject(requiredUri(), configuredDatabaseName());
    }

    private static String requiredUri() {
        final String uri = System.getenv(URI_ENV);
        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException("Set the " + URI_ENV + " environment variable before starting FlushID.");
        }
        return uri;
    }

    private static String configuredDatabaseName() {
        final String name = System.getenv(DATABASE_ENV);
        final String result;
        if (name == null || name.isBlank()) {
            result = "FlushID";
        }
        else {
            result = name;
        }
        return result;
    }

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public MongoDatabase database() {
        return database;
    }

    /**
     * Fails at startup instead of waiting for the first user action to discover a bad connection.
     */
    public void verifyConnection() {
        database.runCommand(new Document("ping", 1));
    }

    @Override
    public void close() {
        if (ownsClient && client != null) {
            client.close();
        }
    }
}
