package data_access.building;

import com.mongodb.client.model.Filters;
import data_access.Condition;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import entity.building.Building;
import entity.building.GenericBuildingFactory;
import data_access.DBDataAccessObject;

import java.util.*;

public class DBBuildingDataAccessObject extends DBDataAccessObject {

	static MongoCollection<Document> collection;
	
	public DBBuildingDataAccessObject() {
		super();    // initializes the MongoClient and MongoDatabase from
					// the set URI
		collection = database.getCollection("Buildings");
	}

	/**
	* Returns all buildings who satisfy all the given conditions
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
	 * Returns all buildings which satisfy all the given conditions, with database IDs
	 * @param conditions a list of condition objects that the returned buildings must satisfy
	 * @return The buildings that match all the conditions mapped to their IDs in the database.
	 */
	public static Map<String, Building> getMatchingIDMap(Iterable<Condition<?>> conditions) {

		Bson filter = parseConditions(conditions);
		List<Document> docs = getAll(filter);

		Map<String, Building> buildings = new HashMap<>();
		for (Document doc: docs) {
			Building building = createBuilding(doc);
			buildings.put(doc.getString("_id"), building);
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
				getLatitude(doc),
				getLongitude(doc),
				doc.getString("controlInfo"));
		return building;
	}

	/**
	 * Writes a single Building object to the database. Latitude and Longitude are converted
	 * to a location object for geospatial filtering
	 * @param building The Building object to be written.
	 * @return the ID for the written object.
	 */
	public String write(Building building) {
		Document doc = new Document();
		doc.append("buildingCode", building.getBuildingCode());
		doc.append("shortName", building.getBuildingNameShort());
		doc.append("longName", building.getBuildingNameLong());
		doc.append("location", createLocation(building));
		doc.append("controlInfo", building.getControlInfo());

		return collection.insertOne(doc).getInsertedId().toString();
	}

	/**
	 * Parse Latitude and Longitude of a Building into a Document
	 * @param building Building object with longitude and latitude
	 * @return Document that can be inserted into the database and used for geospatial filters
	 */
	private static Document createLocation(Building building) {
		Document location = new Document("type", "Point");
		List<Double> coords = new ArrayList<>();
		coords.add(0, building.getLongitude());
		coords.add(1, building.getLatitude());
		location.append("coordinates", coords);
		return location;
	}

	/**
	 * Return the longitude as a double parsed from a Document containing
	 * geospatial data
	 * @param doc Document of valid format:
	 *            type: "Point"
	 *            coordinates: [longitude, latitude]
	 * @return Double representing longitude
	 */
	private static double getLongitude(Document doc) {
		Document location = doc.get("location", doc.getClass());
		return location.getList("coordinates", double.class).get(0);
	}

	/**
	 * Return the latitude as a double parsed from a Document containing
	 * geospatial data
	 * @param doc Document of valid format:
	 *            {type: "Point"
	 *            coordinates: [longitude, latitude]}
	 * @return Double representing latitude
	 */
	private static double getLatitude(Document doc) {
		Document location = doc.get("location", doc.getClass());
		return location.getList("coordinates", double.class).get(1);
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
