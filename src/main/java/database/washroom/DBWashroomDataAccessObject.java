package database.washroom;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import database.AbstractCondition;
import database.CollectionCondition;
import database.Condition;
import database.DBDataAccessObject;
import database.MongoDocuments;
import database.Operator;
import database.building.DBBuildingDataAccessObject;
import entity.ReviewSummary;
import entity.Washroom;
import use_case.filter.WashroomFilterCriteria;
import use_case.filter.WashroomFilterRepository;

public class DBWashroomDataAccessObject extends DBDataAccessObject implements WashroomFilterRepository {

    static final List<String> allowedAttributes = List.of(
        new String[] {"buildingID", "buildingCode", "seedKey", "name", "floor", "gender", "accessible", "numToilets",
            "numSinks", "locationDescription"});
    private final MongoCollection<Document> collection;
    private final MongoCollection<Document> buildings;
    private final MongoCollection<Document> reviews;

    public DBWashroomDataAccessObject() {
        super();    // initializes the MongoClient and MongoDatabase from
        // the set URI
        collection = database.getCollection("Washrooms");
        buildings = database.getCollection("Buildings");
        reviews = database.getCollection("Reviews");
    }

    public DBWashroomDataAccessObject(final MongoDatabase database) {
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
    private static Bson parseConditions(final Iterable<AbstractCondition<?>> conditions) {
        final List<Bson> filters = new ArrayList<>();
        conditions.forEach((condition) -> {
            filters.add(condition.getFilter());
        });
        return filters.isEmpty() ? new Document() : Filters.and(filters);
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
     * Creates a washroom object out of the inputted Document
     *
     * @param doc Document containing washroom data for a specific washroom
     * @return the washroom object constructed using that data
     */
    private Washroom createWashroom(final Document doc) {
        return hydrate(List.of(doc)).getFirst();
    }

    /**
     * Hydrates a result set with one building read and one review-summary aggregation.
     */
    private List<Washroom> hydrate(final List<Document> washroomDocuments) {
        if (washroomDocuments.isEmpty()) {
            return List.of();
        }
        final Map<String, Document> buildingsById = new HashMap<>();
        final Map<String, Document> buildingsByCode = new HashMap<>();
        for (final Document building : buildings.find()) {
            buildingsById.put(MongoDocuments.id(building), building);
            buildingsByCode.put(MongoDocuments.string(building, "", "buildingCode", "code"), building);
        }
        final Set<String> washroomIds = washroomDocuments
            .stream()
            .map(MongoDocuments::id)
            .collect(java.util.stream.Collectors.toSet());
        final Map<String, ReviewSummary> summaries = reviewSummaries(washroomIds);
        return washroomDocuments
            .stream()
            .map(document -> {
                return createWashroom(document, buildingsById, buildingsByCode, summaries);
            })
            .toList();
    }

    private Washroom createWashroom(final Document doc, final Map<String, Document> buildingsById,
                                    final Map<String, Document> buildingsByCode,
                                    final Map<String, ReviewSummary> summaries) {
        final String id = MongoDocuments.id(doc);
        final String buildingId = MongoDocuments.string(doc, "", "buildingID", "buildingId");
        Document buildingDocument = buildingsById.get(buildingId);
        if (buildingDocument == null) {
            buildingDocument = buildingsByCode.get(MongoDocuments.string(doc, buildingId, "buildingCode"));
        }
        if (buildingDocument == null) {
            throw new IllegalStateException("Washroom " + id + " references a missing building.");
        }
        final entity.Building building = toApplicationBuilding(buildingDocument);
        final String floor = MongoDocuments.string(doc, "Unknown floor", "floor");
        final String name = MongoDocuments.string(doc, building.name() + ", " + floor, "name");
        final entity.Washroom.Gender gender = parseGender(MongoDocuments.string(doc, "ALL_GENDER", "gender"));
        return new Washroom(id, name, building, floor, MongoDocuments.bool(doc, false, "accessible"), gender,
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
    private static void checkAttribute(final Iterable<AbstractCondition<?>> conditions) {
        for (final AbstractCondition<?> condition : conditions) {
            checkAttribute(condition);
        }
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
     * Deletes every entry in the database that matches the given conditions
     *
     * @param conditions List of AbstractCondition objects. An object must satisfy
     *                   all conditions to be deleted
     */
    public void delete(final Iterable<AbstractCondition<?>> conditions) {
        checkAttribute(conditions);
        final Bson filter = parseConditions(conditions);
        collection.deleteMany(filter);
    }

    private Map<String, ReviewSummary> reviewSummaries(final Set<String> washroomIds) {
        if (washroomIds.isEmpty()) {
            return Map.of();
        }
        final Document washroomReference = new Document("$ifNull", List.of("$washroomId", "$washroomID"));
        final Document rating = new Document("$ifNull", List.of("$rating", "$stars"));
        final Document cleanliness = new Document("$ifNull", List.of("$cleanliness", rating));
        final List<Document> pipeline = List.of(new Document("$match",
                Filters.or(Filters.in("washroomId", washroomIds), Filters.in("washroomID", washroomIds))),
            new Document("$group", new Document("_id", washroomReference)
                .append("rating", new Document("$avg", rating))
                .append("cleanliness", new Document("$avg", cleanliness))
                .append("count", new Document("$sum", 1))));
        final Map<String, ReviewSummary> summaries = new HashMap<>();
        for (final Document summary : reviews.aggregate(pipeline)) {
            final String washroomId = String.valueOf(summary.get("_id"));
            summaries.put(washroomId, new ReviewSummary(clampRating(MongoDocuments.number(summary, 0, "rating")),
                clampRating(MongoDocuments.number(summary, 0, "cleanliness")),
                Math.max(0, MongoDocuments.integer(summary, 0, "count"))));
        }
        return summaries;
    }

    private static entity.Building toApplicationBuilding(final Document document) {
        final Document location = document.get("location", Document.class);
        final List<?> coordinates =
            location == null ? List.of() : location.getList("coordinates", Object.class, List.of());
        final double longitude =
            coordinates.size() > 0 && coordinates.get(0) instanceof final Number number ? number.doubleValue() :
                MongoDocuments.number(document, 0, "longitude", "lng");
        final double latitude =
            coordinates.size() > 1 && coordinates.get(1) instanceof final Number number ? number.doubleValue() :
                MongoDocuments.number(document, 0, "latitude", "lat");
        final String code = MongoDocuments.string(document, MongoDocuments.id(document), "buildingCode", "code");
        final String name = MongoDocuments.string(document, code, "longName", "shortName", "name");
        return new entity.Building(code, name, latitude, longitude);
    }

    private static entity.Washroom.Gender parseGender(final String value) {
        final String normalized = value
            .trim()
            .toUpperCase(Locale.ROOT)
            .replace('-', '_')
            .replace(' ', '_');
        try {
            return entity.Washroom.Gender.valueOf(normalized);
        }
        catch (final IllegalArgumentException ignored) {
            return entity.Washroom.Gender.ALL_GENDER;
        }
    }

    private static double clampRating(final double value) {
        return Math.max(1, Math.min(5, value));
    }

    private static double distance(final double lat1, final double lon1, final double lat2, final double lon2) {
        final double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        final double y = Math.toRadians(lat2 - lat1);
        return Math.sqrt(x * x + y * y) * 6_371_000;
    }

    public static List<entity.Washroom> loadWashrooms(final String filename) throws Exception {
        final List<entity.Washroom> washroomList = new ArrayList<>();
        try (final Reader reader = new FileReader(filename); final JsonReader jsonReader = Json.createReader(reader)) {

            final JsonArray jsonArray = jsonReader.readArray();

            final DBBuildingDataAccessObject dbBuildingDataAccessObject = new DBBuildingDataAccessObject();

            for (final JsonValue value : jsonArray) {
                final JsonObject obj = (JsonObject) value;

                final String id = obj.getString("ID");
                final String name = obj.getString("name");
                final String genderStr = obj.getString("gender");
                entity.Washroom.Gender gender = null;
                if (genderStr.equals(entity.Washroom.Gender.MEN.toString())) {
                    gender = entity.Washroom.Gender.MEN;
                }
                else if (genderStr.equals(entity.Washroom.Gender.WOMEN.toString())) {
                    gender = entity.Washroom.Gender.WOMEN;
                }
                else if (genderStr.equals("ALL_GENDER")) {
                    gender = entity.Washroom.Gender.ALL_GENDER;
                }
                else {
                    gender = entity.Washroom.Gender.ALL_GENDER;
                }

                final boolean accessible = obj.getBoolean("accessible");
                final Condition condition = new Condition("buildingCode", Operator.EQ, id);
                final List<entity.Building> matchingBuildings = dbBuildingDataAccessObject.getMatching(condition);
                if (!matchingBuildings.isEmpty()) {
                    washroomList.add(
                        new entity.Washroom(id, name, (matchingBuildings.getFirst()), "No data", accessible, gender, 0,
                            0, "Example description", new ReviewSummary(0, 0, 0)));
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
    public String write(final Washroom washroom, final String buildingID) {
        if (washroom instanceof final entity.Washroom applicationWashroom) {
            final Document doc = new Document();
            doc.append("buildingID", buildingID);
            doc.append("buildingCode", applicationWashroom
                .building()
                .code());
            doc.append("seedKey", "campus-" + applicationWashroom
                .building()
                .code()
                .toLowerCase(Locale.ROOT) + "-washroom");
            doc.append("name", applicationWashroom.name());
            doc.append("floor", applicationWashroom.floor());
            doc.append("gender", applicationWashroom
                .gender()
                .name());
            doc.append("accessible", applicationWashroom.accessible());
            doc.append("numToilets", applicationWashroom.numToilets());
            doc.append("numSinks", applicationWashroom.numSinks());
            doc.append("locationDescription", applicationWashroom.locationDescription());
            return collection
                .insertOne(doc)
                .getInsertedId()
                .toString();
        }
        final Document doc = new Document();
        doc.append("buildingID", buildingID);
        doc.append("floor", washroom.floor());

        return collection
            .insertOne(doc)
            .getInsertedId()
            .toString();
    }

    public void main(final String[] args) {
        try {
            final DBWashroomDataAccessObject dbwashroomDAO = new DBWashroomDataAccessObject();
            final Condition condition = new Condition<>("floor", Operator.NE, "00");
            dbwashroomDAO.delete(condition);
            final List<entity.Washroom> washroomList = loadWashrooms("src/main/resources/data/washrooms.json");
            for (final entity.Washroom washroom : washroomList) {
                write(washroom, washroom.id());
                System.out.println(washroom);

            }

        }
        catch (final Exception e) {
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
    public List<entity.Washroom> getMatching(final Iterable<AbstractCondition<?>> conditions) {
        return new ArrayList<>(getMatchingIDMap(conditions).values());

    }

    /**
     * Translates the filter use case's query vocabulary into MongoDB filters.
     * Mongo types do not cross this adapter boundary.
     */
    @Override
    public List<Washroom> findMatching(final WashroomFilterCriteria criteria) {
        final List<AbstractCondition<?>> conditions = new ArrayList<>();
        if (criteria.accessibleOnly()) {
            conditions.add(new Condition<>("accessible", Operator.EQ, true));
        }
        if (criteria.gender() != null) {
            List<String> genders = new ArrayList<>();
            criteria.gender().forEach(gender -> genders.add(gender.name()));
            conditions.add(new CollectionCondition<>("gender", Operator.IN, genders));
        }
        if (criteria.buildingCode() != null && !criteria
            .buildingCode()
            .isBlank()) {
            conditions.add(new Condition<>("buildingCode", Operator.EQ, criteria.buildingCode()));
        }
        if (!criteria
            .permittedNames()
            .isEmpty()) {
            conditions.add(new CollectionCondition<>("name", Operator.IN, criteria.permittedNames()));
        }
        return getMatching(conditions);
    }

    /**
     * Returns all buildings who satisfy the condition
     *
     * @param condition a condition object that the returned buildings must satisfy
     * @return The buildings that match the conditions
     */
    public List<Washroom> getMatching(final AbstractCondition<?> condition) {

        final List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        return getMatching(conditions);

    }

    /**
     * Returns all washrooms which satisfy all the given conditions, with database IDs
     *
     * @param conditions a list of condition objects that the returned washrooms must satisfy
     * @return The washrooms that match all the conditions mapped to their IDs in the database.
     */
    public Map<String, Washroom> getMatchingIDMap(final Iterable<AbstractCondition<?>> conditions) {
        checkAttribute(conditions);

        final Bson filter = parseConditions(conditions);
        final List<Document> docs = getAll(filter);

        final List<Washroom> sortedWashrooms = hydrate(docs)
            .stream()
            .sorted(Comparator
                .comparing((Washroom washroom) -> {
                    return washroom
                        .building()
                        .name();
                }, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Washroom::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Washroom::id))
            .toList();
        final Map<String, Washroom> washrooms = new LinkedHashMap<>();
        for (final Washroom washroom : sortedWashrooms) {
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
    public Map<String, Washroom> getMatchingIDMap(final AbstractCondition<?> condition) {

        final List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        return getMatchingIDMap(conditions);

    }

    /**
     * Deletes every entry in the database that matches the given condition
     *
     * @param condition A AbstractCondition object that the object must satisfy.
     */
    public void delete(final AbstractCondition<?> condition) {
        final List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        delete(conditions);

    }

    @Override
    public Optional<entity.Washroom> getById(final String id) {
        final Document document = MongoDocuments.findById(collection, id);
        return document == null ? Optional.empty() : Optional.of(createWashroom(document));
    }

    @Override
    public List<Washroom> getByIds(final Collection<String> ids) {
        final List<Object> databaseIds = ids
            .stream()
            .filter(Objects::nonNull)
            .filter(id -> {
                return !id.isBlank();
            })
            .flatMap(id -> {
                return ObjectId.isValid(id) ? java.util.stream.Stream.<Object>of(id, new ObjectId(id)) : java.util.stream.Stream.<Object>of(id);
            })
            .distinct()
            .toList();
        if (databaseIds.isEmpty()) {
            return List.of();
        }

        final Map<String, Washroom> washroomsById = hydrate(getAll(Filters.in("_id", databaseIds)))
            .stream()
            .collect(java.util.stream.Collectors.toMap(Washroom::id, washroom -> {
                return washroom;
            }));
        return ids
            .stream()
            .map(washroomsById::get)
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public List<entity.Washroom> getAll() {
        return getMatching(List.of())
            .stream()
            .map(washroom -> {
                return washroom;
            })
            .toList();
    }

    /**
     * Fetches only known JSON-backed washrooms, while retaining batched hydration.
     */
    public List<entity.Washroom> getByNames(final Collection<String> names) {
        if (names.isEmpty()) {
            return List.of();
        }
        return getMatching(List.of(new CollectionCondition<>("name", Operator.IN, names)))
            .stream()
            .map(washroom -> {
                return washroom;
            })
            .toList();
    }

    @Override
    public List<entity.Washroom> getNearby(final double latitude, final double longitude, final double radiusMeters) {
        return getAll()
            .stream()
            .filter(washroom -> {
                return distance(latitude, longitude, washroom
                    .building()
                    .latitude(), washroom
                    .building()
                    .longitude()) <= radiusMeters;
            })
            .toList();
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
