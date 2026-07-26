package data_access;

import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DBDataAccessObject {

	final public String uri = "mongodb+srv://eleanorneal_db_user:lNth5u9FuYk4NzPN@flushid.jpqnasb.mongodb.net/?appName=FlushID";
	final public MongoClient client;
	final public MongoDatabase database;
	
	public DBDataAccessObject() {
	
		client = MongoClients.create(uri);
		try {
			database = client.getDatabase("FlushID");
		}
		
		catch (RuntimeException e) {
			client.close();
			throw new RuntimeException(e);
		}
	}
	
}
