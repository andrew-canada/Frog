package data_access.building;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;
import com.mongodb.client.MongoCollection;

import entity.Building;
import entity.GenericBuildingFactory;
import data_access.DBDataAccessObject;

import java.util.ArrayList;
import java.util.List;

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
	public static List<Building> getMatching(String fieldName, String value) {

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
	private static List<Document> getAll(String fieldName, String value) {
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

	public static boolean in(String fieldName, String value) {

	}



}
