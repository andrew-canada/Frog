package data_access.building;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;
import com.mongodb.client.MongoCollection;

import entity.Building;
import entity.GenericBuildingFactory;
import data_access.DBDataAccessObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBBuildingDataAccessObject extends DBDataAccessObject {

	MongoCollection<Document> collection;
	
	public DBBuildingDataAccessObject() {
		super();    // initializes the MongoClient and MongoDatabase from
					// the set URI
		collection = database.getCollection("Buildings");
	}

	/**
	* Returns all buildings who have the specified value in fieldName
	* @param fieldName the database field name to compare to
	* @param val the value of that field
	* @return The buildings that match the filter
	*/
	public static <T> List<Building> getMatching(String fieldName, T value) {

		List<Document> docs = getAll(fieldName, value);

		List<Building> buildings = new ArrayList<>();
		for (Document doc: docs) {
			Building building = createBuilding(doc);
			buildings.add(building);
		}
		return buildings;

	}

	/**
	 * Return a list of Documents which match the specified parameters
	 * @param fieldName The field in the database to compare the value to
	 * @param value The value the field must equal for the Document to be returned
	 * @return The list of valid documents
	 */
	private static <T> List<Document> getAll(String fieldName, T value) {
		List<Document> docs = new ArrayList<>();
		return collection.find(eq(fieldName, value)).into(docs);
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

	/**
	 * Return whether the given value is a value for fieldName.
	 * @param fieldName The database field to assess.
	 * @param value The value to look for.
	 * @return True if the value is in the field, False otherwise.
	 * @param <T> The type of the value, assuming all values of fieldName use the same type.
	 */
	public <T> boolean in(String fieldName, T value) {

        Set<Object> values = getDistinct(fieldName);
		return ((Set<T>) values).contains(value);
	}

	/**
	 * Get all unique values of fieldName.
	 * @param fieldName The database field name to use.
	 * @return The set of all unique values.
	 */
	private Set<Object> getDistinct(String fieldName) {
		Set<Object> values = new HashSet<>();
		values = collection.distinct(fieldName, value.getClass()).into(values);
		return values
	}

	public void write(Building building) {
	}

	public void writeAll(Collection<Building> buildings) {
	}

	public void delete(Building) {
	}



}
