import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;

import database.AbstractCondition;
import database.Condition;
import database.DBDataAccessObject;
import database.Operator;
import database.building.DBBuildingDataAccessObject;
import database.enrollment.DBEnrollmentDataAccessObject;
import database.review.DBReviewDataAccessObject;
import database.status.DBStatusReportDataAccessObject;
import database.user.DBUserDataAccessObject;
import database.washroom.DBWashroomDataAccessObject;
import entity.Building;
import entity.Report;
import entity.Review;
import entity.StatusReport;
import entity.User;
import entity.Washroom;

/**
 * Exercises the Mongo adapters against a deterministic in-memory Mongo-shaped
 * test double. No Mongo server or network connection is required.
 */
class DatabaseMongoCoverageTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 10, 12, 0);

    @Test
    void baseDaoAndBuildingAdapterUseInjectedDatabase(@TempDir final Path temp) throws Exception {
        final FakeMongo mongo = new FakeMongo();
        final Document building = buildingDocument("b1", "BA", "Bahen Centre", 43.66, -79.39);
        mongo.documents("Buildings").add(building);
        final MongoDatabase database = mongo.database();
        final TestDao dao = new TestDao(database);
        assertEquals(database, dao.database());
        dao.verifyConnection();
        dao.close();

        final DBBuildingDataAccessObject buildings = new DBBuildingDataAccessObject(mongo.database());
        final List<Building> found = buildings.getMatching(new Condition<>("buildingCode", Operator.EQ, "BA"));
        assertEquals(1, found.size());
        assertEquals("Bahen Centre", found.getFirst().name());
        assertEquals("b1", buildings.getMatchingIDMap(new Condition<>("buildingCode", Operator.EQ, "BA"))
            .keySet().iterator().next());
        assertEquals(1, buildings.ensureLocations(List.of(new Building("BA", "Bahen Centre", 43.66, -79.39)))
            .size());
        buildings.delete(new Condition<>("buildingCode", Operator.EQ, "BA"));
        assertThrowsRuntime(() -> buildings.getMatching(new Condition<>("notAllowed", Operator.EQ, "x")));

        final Path json = temp.resolve("buildings.json");
        Files.writeString(json, "[{\"ID\":\"BA\",\"name\":\"Bahen\",\"latitude\":\"43.66\","
            + "\"longitude\":\"-79.39\"}]");
        assertEquals("BA", DBBuildingDataAccessObject.loadBuildings(json.toString()).getFirst().code());
    }

    @Test
    void userAndEnrollmentAdaptersReadAndMaintainApplicationState() {
        final FakeMongo mongo = new FakeMongo();
        mongo.documents("Users").add(new Document("_id", "u1").append("username", "alice")
            .append("passwordHash", "hash").append("personalPlan", "plan").append("isModerator", true));
        mongo.documents("EnrollmentMeetings").add(new Document("buildingCode", "BA")
            .append("dayOfWeek", "MONDAY").append("startHour", 9).append("endHour", 11)
            .append("enrollment", 40));

        final DBUserDataAccessObject users = new DBUserDataAccessObject(mongo.database());
        final List<String> events = new ArrayList<>();
        users.addPropertyChangeListener(event -> events.add(event.getPropertyName()));
        assertEquals("hash", users.get("alice").orElseThrow().passwordHash());
        assertTrue(users.existsByName("alice"));
        assertTrue(users.isModerator("alice"));
        users.save(new User("alice", "new-hash", "new-plan", true));
        users.ensureModerator("alice");
        final User current = new User("alice", "hash", "plan", true);
        users.setCurrentUser(current);
        assertEquals(Optional.of(current), users.currentUser());
        users.clear();
        assertTrue(users.currentUser().isEmpty());
        assertTrue(events.contains("state"));
        users.removeUser("alice");

        final DBEnrollmentDataAccessObject enrollment = new DBEnrollmentDataAccessObject(mongo.database());
        assertEquals(1, enrollment.getBuildingSchedule("BA", DayOfWeek.MONDAY).size());
        assertTrue(enrollment.getBuildingSchedule("BA", DayOfWeek.TUESDAY).size() >= 0);
    }

    @Test
    void statusAdapterCoversPersistenceSeedingQueriesAndIndexes() {
        final FakeMongo mongo = new FakeMongo();
        final Date timestamp = Date.from(NOW.atZone(java.time.ZoneId.systemDefault()).toInstant());
        mongo.documents("StatusReports").add(new Document("_id", "s1").append("washroomId", "w1")
            .append("username", "alice").append("busyness", 7).append("cleanliness", 0)
            .append("issue", "NOT_A_REAL_ISSUE").append("timestamp", timestamp));
        mongo.aggregateDocuments("StatusReports").add(new Document("washroomId", "w1")
            .append("username", "alice").append("busyness", 3).append("cleanliness", 4)
            .append("issue", "NONE").append("timestamp", timestamp));

        final DBStatusReportDataAccessObject statuses = new DBStatusReportDataAccessObject(mongo.database());
        statuses.save(new StatusReport("w1", "alice", 3, 4, entity.MaintenanceIssue.NONE, NOW));
        statuses.ensureJsonHourlyReports(List.of(washroom("w1", "BA")));
        statuses.ensureJsonHourlyReports(List.of(washroom("w1", "BA")));
        assertFalse(statuses.getRecentForWashroom("w1", NOW.minusDays(1)).isEmpty());
        assertFalse(statuses.getForWashroom("w1", NOW.minusDays(1), NOW.plusDays(1)).isEmpty());
        assertTrue(statuses.getCurrentHourForWashrooms(List.of(), 12).isEmpty());
        assertEquals(1, statuses.getCurrentHourForWashrooms(List.of("w1"), 12).size());
        statuses.ensurePerformanceIndexes();
    }

    @Test
    void reviewAdapterCoversReviewsVotesReportsAndSeeds() {
        final FakeMongo mongo = new FakeMongo();
        final Date timestamp = Date.from(NOW.atZone(java.time.ZoneId.systemDefault()).toInstant());
        mongo.documents("Reviews").add(new Document("_id", "r1").append("washroomID", "w1")
            .append("authorUsername", "alice").append("rating", 6).append("cleanliness", 0)
            .append("comment", "good").append("helpfulCount", 2).append("createdAt", timestamp));
        mongo.documents("ReviewVotes").add(new Document("_id", "v1").append("reviewId", "r1")
            .append("username", "alice"));
        mongo.documents("ReviewReports").add(new Document("_id", "p1").append("reviewId", "r1")
            .append("reporterUsername", "bob").append("reasons", List.of("Spam"))
            .append("details", "details").append("createdAt", NOW.toString()));

        final DBReviewDataAccessObject reviews = new DBReviewDataAccessObject(mongo.database());
        assertEquals(1, reviews.getMatching(new Condition<>("rating", Operator.GTE, 1)).size());
        assertEquals(1, reviews.getReviewsForWashroom("w1").size());
        assertEquals(1, reviews.getReviewsByUser("alice").size());
        assertEquals(1, reviews.getSummary("w1").reviewCount());
        assertEquals("alice", reviews.getById("r1").orElseThrow().authorUsername());
        assertTrue(reviews.getById("").isEmpty());
        reviews.save(new Review("r2", "w1", "alice", 4, 4, "saved", 0, LocalDate.now()));
        reviews.save(new Report("p2", "r1", "alice", List.of("Other"), "more", NOW));

        assertTrue(reviews.hasVoted("r1", "alice"));
        assertEquals(Set.of("r1"), reviews.votedReviewIds(List.of("r1"), "alice"));
        assertEquals(Set.of(), reviews.votedReviewIds(List.of(), "alice"));
        reviews.addVote("r1", "alice");
        reviews.removeVote("r1", "alice");
        assertTrue(reviews.hasReported("r1", "bob"));
        assertEquals(Set.of("r1"), reviews.reportedReviewIds(List.of("r1"), "bob"));
        assertEquals(Set.of(), reviews.reportedReviewIds(List.of("r1"), ""));
        assertEquals(2, reviews.getAllReports().size());
        reviews.deleteReportsForReview("r1");
        reviews.deleteReview("r1");
        reviews.ensureJsonReviews(List.of(washroom("w1", "BA"), washroom("w2", "BA"), washroom("w3", "BA")));
        reviews.ensureJsonReviews(List.of(washroom("w1", "BA"), washroom("w2", "BA"), washroom("w3", "BA")));
        reviews.ensurePerformanceIndexes();
        reviews.delete(new Condition<>("rating", Operator.EQ, 4));
    }

    @Test
    void washroomAdapterHydratesBuildingsAndReviewSummaries() {
        final FakeMongo mongo = new FakeMongo();
        mongo.documents("Buildings").add(buildingDocument("b1", "BA", "Bahen Centre", 43.66, -79.39));
        mongo.documents("Washrooms").add(new Document("_id", "w1").append("buildingID", "b1")
            .append("buildingCode", "BA").append("name", "Main washroom").append("floor", "2")
            .append("gender", "all-gender").append("accessible", true).append("numToilets", 3)
            .append("numSinks", 2).append("locationDescription", "near elevators"));
        mongo.aggregateDocuments("Reviews").add(new Document("_id", "w1").append("rating", 4.5)
            .append("cleanliness", 4).append("count", 2));

        final DBWashroomDataAccessObject washrooms = new DBWashroomDataAccessObject(mongo.database());
        assertEquals("Main washroom", washrooms.getById("w1").orElseThrow().name());
        assertEquals(1, washrooms.getByIds(java.util.Arrays.asList("w1", "", null)).size());
        assertTrue(washrooms.getByIds(List.of()).isEmpty());
        assertEquals(1, washrooms.getAll().size());
        assertEquals(1, washrooms.getByNames(List.of("Main washroom")).size());
        assertTrue(washrooms.getByNames(List.of()).isEmpty());
        assertEquals(1, washrooms.getNearby(43.66, -79.39, 100).size());
        assertTrue(washrooms.getNearby(0, 0, 1).isEmpty());
        assertEquals(1, washrooms.findMatching(new use_case.filter.WashroomFilterCriteria(true,
            List.of(Washroom.Gender.ALL_GENDER), "BA", Set.of("Main washroom"))).size());
        assertEquals(1, washrooms.findMatching(new use_case.filter.WashroomFilterCriteria(false, null, "", Set.of()))
            .size());
        assertEquals(1, washrooms.getMatching(new Condition<>("buildingCode", Operator.EQ, "BA")).size());
        assertEquals(1, washrooms.getMatchingIDMap(new Condition<>("name", Operator.EQ, "Main washroom")).size());
        assertThrowsRuntime(() -> washrooms.getMatching(new Condition<>("bad", Operator.EQ, "x")));
        washrooms.ensurePerformanceIndexes();
    }

    @Test
    void washroomAdapterHandlesEmptyQueriesAndMissingBuildingReferences() {
        final FakeMongo emptyMongo = new FakeMongo();
        final DBWashroomDataAccessObject empty = new DBWashroomDataAccessObject(emptyMongo.database());
        assertTrue(empty.getAll().isEmpty());
        assertTrue(empty.getById("").isEmpty());
        assertTrue(empty.getByIds(List.of("507f1f77bcf86cd799439011")).isEmpty());

        final FakeMongo missingBuildingMongo = new FakeMongo();
        missingBuildingMongo.documents("Washrooms").add(new Document("_id", "orphan")
            .append("buildingID", "missing-building"));
        assertThrows(IllegalStateException.class,
            () -> new DBWashroomDataAccessObject(missingBuildingMongo.database()).getById("orphan"));
    }

    @Test
    void mongoAdaptersHandleLegacyFieldsAndMissingOptionalCoordinates() {
        final FakeMongo buildingMongo = new FakeMongo();
        buildingMongo.documents("Buildings").add(new Document("_id", "legacy-building")
            .append("shortName", "Legacy Hall").append("latitude", "43.70").append("lng", "-79.41"));
        final Building legacyBuilding = new DBBuildingDataAccessObject(buildingMongo.database())
            .getMatching(new Condition<>("shortName", Operator.EQ, "Legacy Hall"))
            .getFirst();
        assertEquals("legacy-building", legacyBuilding.code());
        assertEquals(43.70, legacyBuilding.latitude());
        assertEquals(-79.41, legacyBuilding.longitude());

        final FakeMongo seedMongo = new FakeMongo();
        final List<Building> persisted = new DBBuildingDataAccessObject(seedMongo.database()).ensureLocations(
            List.of(new Building("NEW", "New Hall", 43.71, -79.42)));
        assertEquals(1, persisted.size());

        final FakeMongo washroomMongo = new FakeMongo();
        washroomMongo.documents("Buildings").add(new Document("_id", "fallback-building")
            .append("buildingCode", "FB").append("name", "Fallback Hall").append("latitude", "43.72")
            .append("longitude", "-79.43"));
        washroomMongo.documents("Washrooms").add(new Document("_id", "fallback-washroom")
            .append("buildingCode", "FB").append("gender", "not-a-gender"));
        final Washroom fallback = new DBWashroomDataAccessObject(washroomMongo.database())
            .getById("fallback-washroom")
            .orElseThrow();
        assertEquals("FB", fallback.building().code());
        assertEquals(Washroom.Gender.NO_INFO, fallback.gender());
        assertEquals("Fallback Hall, Unknown floor", fallback.name());
        assertFalse(fallback.accessible());

        final FakeMongo reviewMongo = new FakeMongo();
        reviewMongo.documents("Users").add(new Document("_id", "legacy-user").append("name", "legacy"));
        reviewMongo.documents("Reviews").add(new Document("_id", "legacy-review").append("washroomId", "w1")
            .append("userId", "legacy-user").append("stars", 6).append("text", "legacy text")
            .append("helpfuls", -2).append("date", "not-a-date"));
        reviewMongo.documents("ReviewReports").add(new Document("_id", "legacy-report")
            .append("reviewId", "legacy-review").append("username", "reporter").append("details", ""));
        final DBReviewDataAccessObject reviews = new DBReviewDataAccessObject(reviewMongo.database());
        final Review legacyReview = reviews.getById("legacy-review").orElseThrow();
        assertEquals("legacy", legacyReview.authorUsername());
        assertEquals(5, legacyReview.rating());
        assertEquals(0, legacyReview.helpfulCount());
        assertEquals(1, reviews.getAllReports().size());

        final FakeMongo anonymousMongo = new FakeMongo();
        anonymousMongo.documents("Reviews").add(new Document("_id", "anonymous-review")
            .append("washroomId", "w1").append("userId", "missing-user").append("rating", 3)
            .append("cleanliness", 3));
        assertEquals("Anonymous", new DBReviewDataAccessObject(anonymousMongo.database())
            .getById("anonymous-review").orElseThrow().authorUsername());
        assertTrue(new DBReviewDataAccessObject(new FakeMongo().database()).getSummary("missing").reviewCount() == 0);

        final FakeMongo userMongo = new FakeMongo();
        userMongo.documents("Users").add(new Document("_id", "legacy-user").append("name", "legacy")
            .append("password", "hash"));
        final DBUserDataAccessObject users = new DBUserDataAccessObject(userMongo.database());
        assertEquals("legacy", users.get("legacy").orElseThrow().username());
    }

    private static Document buildingDocument(final String id, final String code, final String name,
                                             final double latitude, final double longitude) {
        return new Document("_id", id).append("buildingCode", code).append("longName", name)
            .append("location", new Document("coordinates", List.of(longitude, latitude)));
    }

    private static Washroom washroom(final String id, final String buildingCode) {
        return new Washroom(id, "Washroom", new Building(buildingCode, "Building", 43.66, -79.39), "1", true,
            Washroom.Gender.ALL_GENDER, 2, 2, "inside", entity.ReviewSummary.empty());
    }

    private static void assertThrowsRuntime(final Runnable action) {
        try {
            action.run();
        }
        catch (final RuntimeException expected) {
            return;
        }
        throw new AssertionError("expected RuntimeException");
    }

    private static final class TestDao extends DBDataAccessObject {
        private TestDao(final MongoDatabase database) {
            super(database);
        }
    }

    private static final class FakeMongo {
        private final Map<String, List<Document>> documents = new HashMap<>();
        private final Map<String, List<Document>> aggregateDocuments = new HashMap<>();
        private int generatedId;

        private List<Document> documents(final String collection) {
            return documents.computeIfAbsent(collection, ignored -> new ArrayList<>());
        }

        private List<Document> aggregateDocuments(final String collection) {
            return aggregateDocuments.computeIfAbsent(collection, ignored -> new ArrayList<>());
        }

        private MongoDatabase database() {
            final InvocationHandler handler = (proxy, method, args) -> {
                if (method.getName().equals("getCollection")) {
                    return collection((String) args[0]);
                }
                if (method.getName().equals("runCommand")) {
                    return new Document("ok", 1);
                }
                if (method.getName().equals("equals")) {
                    return proxy == args[0];
                }
                if (method.getName().equals("hashCode")) {
                    return System.identityHashCode(proxy);
                }
                return defaultValue(method);
            };
            return proxy(MongoDatabase.class, handler);
        }

        @SuppressWarnings("unchecked")
        private MongoCollection<Document> collection(final String name) {
            final InvocationHandler handler = (proxy, method, args) -> {
                final String methodName = method.getName();
                if (methodName.equals("find")) {
                    return iterable(documents(name), com.mongodb.client.FindIterable.class);
                }
                if (methodName.equals("aggregate")) {
                    return iterable(aggregateDocuments.getOrDefault(name, List.of()),
                        com.mongodb.client.AggregateIterable.class);
                }
                if (methodName.equals("insertOne")) {
                    final Document document = (Document) args[0];
                    persist(name, document);
                    return insertResult(document);
                }
                if (methodName.equals("insertMany")) {
                    for (final Document document : (List<Document>) args[0]) {
                        persist(name, document);
                    }
                    return null;
                }
                if (methodName.equals("replaceOne")) {
                    persist(name, (Document) args[1]);
                    return null;
                }
                if (methodName.equals("toString")) {
                    return "FakeMongoCollection(" + name + ")";
                }
                return defaultValue(method);
            };
            return proxy(MongoCollection.class, handler);
        }

        private void persist(final String collection, final Document document) {
            if (!document.containsKey("_id")) {
                document.put("_id", collection + "-" + generatedId++);
            }
            documents(collection).add(document);
        }

        private InsertOneResult insertResult(final Document document) {
            final BsonString id = new BsonString(String.valueOf(document.get("_id")));
            final InvocationHandler handler = (proxy, method, args) -> {
                if (method.getName().equals("getInsertedId")) {
                    return id;
                }
                return defaultValue(method);
            };
            return InsertOneResult.acknowledged(id);
        }

        @SuppressWarnings("unchecked")
        private static <T> T iterable(final List<Document> values, final Class<?> iterableType) {
            final List<Document> snapshot = List.copyOf(values);
            final InvocationHandler handler = (proxy, method, args) -> {
                if (method.getName().equals("into")) {
                    final Collection<Document> target = (Collection<Document>) args[0];
                    target.addAll(snapshot);
                    return target;
                }
                if (method.getName().equals("first")) {
                    return snapshot.isEmpty() ? null : snapshot.getFirst();
                }
                if (method.getName().equals("iterator")) {
                    return cursor(snapshot);
                }
                return defaultValue(method);
            };
            return (T) Proxy.newProxyInstance(DatabaseMongoCoverageTest.class.getClassLoader(),
                new Class<?>[] {iterableType},
                handler);
        }

        private static com.mongodb.client.MongoCursor<Document> cursor(final List<Document> values) {
            final int[] index = {0};
            final InvocationHandler handler = (proxy, method, args) -> {
                if (method.getName().equals("hasNext")) {
                    return index[0] < values.size();
                }
                if (method.getName().equals("next")) {
                    return values.get(index[0]++);
                }
                if (method.getName().equals("tryNext")) {
                    return index[0] < values.size() ? values.get(index[0]++) : null;
                }
                return defaultValue(method);
            };
            return proxy(com.mongodb.client.MongoCursor.class, handler);
        }

        private static Object defaultValue(final Method method) {
            return defaultValue(method.getReturnType());
        }

        private static Object defaultValue(final Class<?> type) {
            if (!type.isPrimitive()) {
                return null;
            }
            if (type == boolean.class) {
                return false;
            }
            if (type == char.class) {
                return '\0';
            }
            if (type == byte.class) {
                return (byte) 0;
            }
            if (type == short.class) {
                return (short) 0;
            }
            if (type == int.class) {
                return 0;
            }
            if (type == long.class) {
                return 0L;
            }
            if (type == float.class) {
                return 0F;
            }
            return 0D;
        }

        @SuppressWarnings("unchecked")
        private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
            return (T) Proxy.newProxyInstance(DatabaseMongoCoverageTest.class.getClassLoader(),
                new Class<?>[] {type}, handler);
        }
    }
}
