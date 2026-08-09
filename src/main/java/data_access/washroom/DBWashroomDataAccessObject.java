package data_access.washroom;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import data_access.*;
import data_access.building.DBBuildingDataAccessObject;
import entity.ReviewSummary;
import entity.Washroom;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.json.*;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;

public class DBWashroomDataAccessObject extends DBDataAccessObject implements WashroomDataAccessInterface {

    static final List<String> allowedAttributes = List.of(new String[]{
            "buildingID",
            "buildingCode",
            "seedKey",
            "name",
            "floor",
            "gender",
            "accessible",
            "numToilets",
            "numSinks",
            "locationDescription"});
    static MongoCollection<Document> collection;
    private static MongoCollection<Document> buildings;
    private static MongoCollection<Document> reviews;

    public DBWashroomDataAccessObject() {
        super();    // initializes the MongoClient and MongoDatabase from
        // the set URI
        collection = database.getCollection("Washrooms");
        buildings = database.getCollection("Buildings");
        reviews = database.getCollection("Reviews");
    }

    public DBWashroomDataAccessObject(MongoDatabase database) {
        super(database);
        collection = database.getCollection("Washrooms");
        buildings = database.getCollection("Buildings");
        reviews = database.getCollection("Reviews");
    }

    /**
     * Parses a list of AbstractCondition objects into a single Bson filter
     *
     * @param conditions list of condition objects to be connected by and statements
     * @return a Bson filter representing satisfying all conditions
     */
    private static Bson parseConditions(Iterable<AbstractCondition<?>> conditions) {
        List<Bson> filters = new ArrayList<>();
        conditions.forEach((condition) -> filters.add(condition.getFilter()));
        return filters.isEmpty() ? new Document() : Filters.and(filters);
    }

    /**
     * Return a list of Documents which match the specified parameters
     *
     * @param filter the filter that must be satisfied for the Document to be returned
     * @return The list of valid documents
     */
    private static <T> List<Document> getAll(Bson filter) {
        List<Document> docs = new ArrayList<>();
        return collection.find(filter).into(docs);
    }

    /**
     * Creates a washroom object out of the inputted Document
     *
     * @param doc Document containing washroom data for a specific washroom
     * @return the washroom object constructed using that data
     */
    private static Washroom createWashroom(Document doc) {
        return hydrate(List.of(doc)).getFirst();
    }

    /**
     * Hydrates a result set with one building read and one review-summary aggregation.
     */
    private static List<Washroom> hydrate(List<Document> washroomDocuments) {
        if (washroomDocuments.isEmpty()) return List.of();
        Map<String, Document> buildingsById = new HashMap<>();
        Map<String, Document> buildingsByCode = new HashMap<>();
        for (Document building : buildings.find()) {
            buildingsById.put(MongoDocuments.id(building), building);
            buildingsByCode.put(MongoDocuments.string(building, "", "buildingCode", "code"), building);
        }
        Set<String> washroomIds = washroomDocuments.stream().map(MongoDocuments::id).collect(java.util.stream.Collectors.toSet());
        Map<String, ReviewSummary> summaries = reviewSummaries(washroomIds);
        return washroomDocuments.stream()
                .map(document -> createWashroom(document, buildingsById, buildingsByCode, summaries))
                .toList();
    }

    private static Washroom createWashroom(Document doc, Map<String, Document> buildingsById,
                                           Map<String, Document> buildingsByCode,
                                           Map<String, ReviewSummary> summaries) {
        String id = MongoDocuments.id(doc);
        String buildingId = MongoDocuments.string(doc, "", "buildingID", "buildingId");
        Document buildingDocument = buildingsById.get(buildingId);
        if (buildingDocument == null)
            buildingDocument = buildingsByCode.get(MongoDocuments.string(doc, buildingId, "buildingCode"));
        if (buildingDocument == null)
            throw new IllegalStateException("Washroom " + id + " references a missing building.");
        entity.Building building = toApplicationBuilding(buildingDocument);
        String floor = MongoDocuments.string(doc, "Unknown floor", "floor");
        String name = MongoDocuments.string(doc, building.name() + ", " + floor, "name");
        entity.Washroom.Gender gender = parseGender(MongoDocuments.string(doc, "ALL_GENDER", "gender"));
        return new Washroom(id, name, building, floor,
                MongoDocuments.bool(doc, false, "accessible"), gender,
                Math.max(0, MongoDocuments.integer(doc, 0, "numToilets", "toilets")),
                Math.max(0, MongoDocuments.integer(doc, 0, "numSinks", "sinks")),
                MongoDocuments.string(doc, "Inside " + building.name(), "locationDescription", "description"),
                summaries.getOrDefault(id, ReviewSummary.empty()));
    }

    /**
     * Checks the conditions against the list of allowed attributes, throwing a
     * runtime exception if it's not a valid attribute.
     *
     * @param conditions The conditions to check.
     */
    private static void checkAttribute(Iterable<AbstractCondition<?>> conditions) {
        for (AbstractCondition<?> condition : conditions) {
            checkAttribute(condition);
        }
    }

    /**
     * Checks the condition against the list of allowed attributes, throwing a
     * runtime exception if it's not a valid attribute.
     *
     * @param condition The condition to check.
     */
    private static void checkAttribute(AbstractCondition<?> condition) {
        if (!allowedAttributes.contains(condition.getFieldName())) {
            throw new RuntimeException("Not a valid attribute");
        }
    }

    /**
     * Deletes every entry in the database that matches the given conditions
     *
     * @param conditions List of AbstractCondition objects. An object must satisfy
     *                   all conditions to be deleted
     */
    public static void delete(Iterable<AbstractCondition<?>> conditions) {
        checkAttribute(conditions);
        Bson filter = parseConditions(conditions);
        collection.deleteMany(filter);
    }

    private static Map<String, ReviewSummary> reviewSummaries(Set<String> washroomIds) {
        if (washroomIds.isEmpty()) return Map.of();
        Document washroomReference = new Document("$ifNull", List.of("$washroomId", "$washroomID"));
        Document rating = new Document("$ifNull", List.of("$rating", "$stars"));
        Document cleanliness = new Document("$ifNull", List.of("$cleanliness", rating));
        List<Document> pipeline = List.of(
                new Document("$match", Filters.or(Filters.in("washroomId", washroomIds), Filters.in("washroomID", washroomIds))),
                new Document("$group", new Document("_id", washroomReference)
                        .append("rating", new Document("$avg", rating))
                        .append("cleanliness", new Document("$avg", cleanliness))
                        .append("count", new Document("$sum", 1))));
        Map<String, ReviewSummary> summaries = new HashMap<>();
        for (Document summary : reviews.aggregate(pipeline)) {
            String washroomId = String.valueOf(summary.get("_id"));
            summaries.put(washroomId, new ReviewSummary(
                    clampRating(MongoDocuments.number(summary, 0, "rating")),
                    clampRating(MongoDocuments.number(summary, 0, "cleanliness")),
                    Math.max(0, MongoDocuments.integer(summary, 0, "count"))));
        }
        return summaries;
    }

    private static entity.Building toApplicationBuilding(Document document) {
        Document location = document.get("location", Document.class);
        List<?> coordinates = location == null ? List.of() : location.getList("coordinates", Object.class, List.of());
        double longitude = coordinates.size() > 0 && coordinates.get(0) instanceof Number number
                ? number.doubleValue() : MongoDocuments.number(document, 0, "longitude", "lng");
        double latitude = coordinates.size() > 1 && coordinates.get(1) instanceof Number number
                ? number.doubleValue() : MongoDocuments.number(document, 0, "latitude", "lat");
        String code = MongoDocuments.string(document, MongoDocuments.id(document), "buildingCode", "code");
        String name = MongoDocuments.string(document, code, "longName", "shortName", "name");
        return new entity.Building(code, name, latitude, longitude);
    }

    private static entity.Washroom.Gender parseGender(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        try {
            return entity.Washroom.Gender.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return entity.Washroom.Gender.ALL_GENDER;
        }
    }

    private static double clampRating(double value) {
        return Math.max(1, Math.min(5, value));
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double y = Math.toRadians(lat2 - lat1);
        return Math.sqrt(x * x + y * y) * 6_371_000;
    }

    public static List<entity.Washroom> loadWashrooms(String filename) throws Exception {
        List<entity.Washroom> washroomList = new ArrayList<>();
        try (Reader reader = new FileReader(filename);
             JsonReader jsonReader = Json.createReader(reader)) {

            JsonArray jsonArray = jsonReader.readArray();

            DBBuildingDataAccessObject dbBuildingDataAccessObject = new DBBuildingDataAccessObject();

            for (JsonValue value : jsonArray) {
                JsonObject obj = (JsonObject) value;

                String id = obj.getString("ID");
                String name = obj.getString("name");
                String genderStr = obj.getString("gender");
                entity.Washroom.Gender gender = null;
                if (genderStr.equals(entity.Washroom.Gender.MEN.toString())) {
                    gender = entity.Washroom.Gender.MEN;
                } else if (genderStr.equals(entity.Washroom.Gender.WOMEN.toString())) {
                    gender = entity.Washroom.Gender.WOMEN;
                } else if (genderStr.equals("ALL_GENDER")) {
                    gender = entity.Washroom.Gender.ALL_GENDER;
                } else {
                    gender = entity.Washroom.Gender.ALL_GENDER;
                }

                boolean accessible = obj.getBoolean("accessible");
                Condition condition = new Condition("buildingCode", Operator.EQ, id);
                List<entity.Building> matchingBuildings = DBBuildingDataAccessObject.getMatching(condition);
                if (!matchingBuildings.isEmpty()) {
                    washroomList.add(new entity.Washroom(
                            id,
                            name,
                            (matchingBuildings.getFirst()),
                            "No data",
                            accessible,
                            gender,
                            0,
                            0,
                            "Example description",
                            new ReviewSummary(0, 0, 0)
                    ));
                }
            }
        }

        return washroomList;
    }

    /**
     * Writes a single Washroom object to the database.
     *
     * @param washroom The Washroom object to be written.
     */
    public String write(Washroom washroom, String buildingID) {
        if (washroom instanceof entity.Washroom applicationWashroom) {
            Document doc = new Document();
            doc.append("buildingID", buildingID);
            doc.append("buildingCode", applicationWashroom.building().code());
            doc.append("seedKey", "campus-" + applicationWashroom.building().code().toLowerCase(Locale.ROOT) + "-washroom");
            doc.append("name", applicationWashroom.name());
            doc.append("floor", applicationWashroom.floor());
            doc.append("gender", applicationWashroom.gender().name());
            doc.append("accessible", applicationWashroom.accessible());
            doc.append("numToilets", applicationWashroom.numToilets());
            doc.append("numSinks", applicationWashroom.numSinks());
            doc.append("locationDescription", applicationWashroom.locationDescription());
            return collection.insertOne(doc).getInsertedId().toString();
        }
        Document doc = new Document();
        doc.append("buildingID", buildingID);
        doc.append("floor", washroom.floor());

        return collection.insertOne(doc).getInsertedId().toString();
    }

    public void main(String[] args) {
        try {
            DBWashroomDataAccessObject dbwashroomDAO = new DBWashroomDataAccessObject();
            Condition condition = new Condition<>("floor", Operator.NE, "00");
            dbwashroomDAO.delete(condition);
            List<entity.Washroom> washroomList = loadWashrooms("src/main/resources/data/washrooms.json");
            for (entity.Washroom washroom : washroomList) {
                write(washroom, washroom.id());
                System.out.println(washroom);

            }

        } catch (Exception e) {
            System.out.println(e);
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
            System.out.println("Error.");
        }


    }

    /**
     * Returns all washrooms who satisfy all the given conditions
     *
     * @param conditions a list of condition objects that the returned washrooms must satisfy
     * @return The washrooms that match all the conditions
     */
    @Override
    public List<entity.Washroom> getMatching(Iterable<AbstractCondition<?>> conditions) {
        return new ArrayList<>(getMatchingIDMap(conditions).values());

    }

    /**
     * Returns all buildings who satisfy the condition
     *
     * @param condition a condition object that the returned buildings must satisfy
     * @return The buildings that match the conditions
     */
    public List<Washroom> getMatching(AbstractCondition<?> condition) {

        List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        return getMatching(conditions);

    }

    /**
     * Returns all washrooms which satisfy all the given conditions, with database IDs
     *
     * @param conditions a list of condition objects that the returned washrooms must satisfy
     * @return The washrooms that match all the conditions mapped to their IDs in the database.
     */
    @Override
    public Map<String, Washroom> getMatchingIDMap(Iterable<AbstractCondition<?>> conditions) {
        checkAttribute(conditions);

        Bson filter = parseConditions(conditions);
        List<Document> docs = getAll(filter);

        List<Washroom> sortedWashrooms = hydrate(docs).stream()
                .sorted(Comparator.comparing((Washroom washroom) -> washroom.building().name(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Washroom::name, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Washroom::id))
                .toList();
        Map<String, Washroom> washrooms = new LinkedHashMap<>();
        for (Washroom washroom : sortedWashrooms) {
            washrooms.put(washroom.id(), washroom);
        }
        return washrooms;
    }

    /**
     * Returns all buildings which satisfy the given condition, with database IDs
     *
     * @param condition a condition object that the returned buildings must satisfy
     * @return The buildings that match the condition mapped to their IDs in the database.
     */
    public Map<String, Washroom> getMatchingIDMap(AbstractCondition<?> condition) {

        List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        return getMatchingIDMap(conditions);

    }

    /**
     * Deletes every entry in the database that matches the given condition
     *
     * @param condition A AbstractCondition object that the object must satisfy.
     */
    public void delete(AbstractCondition<?> condition) {
        List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        delete(conditions);

    }

    @Override
    public Optional<entity.Washroom> getById(String id) {
        Document document = MongoDocuments.findById(collection, id);
        return document == null ? Optional.empty() : Optional.of(createWashroom(document));
    }

    @Override
    public List<entity.Washroom> getAll() {
        return getMatching(List.of()).stream().map(washroom -> washroom).toList();
    }

    /**
     * Fetches only known JSON-backed washrooms, while retaining batched hydration.
     */
    public List<entity.Washroom> getByNames(Collection<String> names) {
        if (names.isEmpty()) return List.of();
        return getMatching(List.of(new CollectionCondition<>("name", Operator.IN, names))).stream()
                .map(washroom -> washroom)
                .toList();
    }

    @Override
    public List<entity.Washroom> getNearby(double latitude, double longitude, double radiusMeters) {
        return getAll().stream().filter(washroom -> distance(latitude, longitude,
                washroom.building().latitude(), washroom.building().longitude()) <= radiusMeters).toList();
    }

    /**
     * Creates indexes used by the primary map and filter queries.
     */
    public void ensurePerformanceIndexes() {
        collection.createIndex(Indexes.ascending("name"));
        collection.createIndex(Indexes.ascending("accessible"));
        collection.createIndex(Indexes.ascending("gender"));
        collection.createIndex(Indexes.ascending("buildingCode"));
    }
}
