package data_access.building;

import com.mongodb.client.model.Filters;
import data_access.Condition;
import data_access.Operator;
import org.bson.Document;
import com.mongodb.client.MongoCollection;

import entity.Building;
import entity.GenericBuildingFactory;
import data_access.DBDataAccessObject;
import org.bson.conversions.Bson;

import java.util.*;

public class DBBuildingDataAccessObject extends DBDataAccessObject {

	static MongoCollection<Document> collection;
	
	public DBBuildingDataAccessObject() {
		super();    // initializes the MongoClient and MongoDatabase from
					// the set URI
		collection = database.getCollection("Buildings");
	}

	/**
	* Returns all buildings who have the specified value in fieldName
	* @param conditions a list of condition objects that the returned buildings must satisfy
	* @return The buildings that match all the conditions
	*/
	public static List<Building> getMatching(Iterable<Condition<?>> conditions) {

		Bson filter = parseConditions(conditions);
		List<Document> docs = getAll(filter);

		List<Building> buildings = new ArrayList<>();
		for (Document doc: docs) {
			Building building = createBuilding(doc);
			buildings.add(building);
		}
		return buildings;

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
	 * Creates a Building object out of the inputted Document
	 * @param doc Document containing building data for a specific building
	 * @return the Building object constructed using that data
	 */
	private static Building createBuilding(Document doc) {
		Building building = GenericBuildingFactory.create(
				doc.getString("buildingCode"),
				doc.getString("shortName"),
				doc.getString("longName"),
				doc.getDouble("latitude"),
				doc.getDouble("longitude"),
				doc.getString("controlInfo"));
		return building;
	}

	public void write(Building building) {
	}

	public void writeAll(Collection<Building> buildings) {
	}

	public void delete(Building building) {
	}



}
