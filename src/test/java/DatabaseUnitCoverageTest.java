import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import database.CollectionCondition;
import database.Condition;
import database.MongoDocuments;
import database.Operator;
import database.review.InMemoryReviewDataAccessObject;
import database.security.BcryptPasswordHasher;
import database.status.InMemoryStatusReportDataAccessObject;
import database.user.InMemoryUserDataAccessObject;
import database.washroom.InMemoryWashroomDataAccessObject;
import entity.Report;
import entity.Review;
import entity.ReviewSummary;
import entity.StatusReport;
import entity.User;
import entity.Washroom;
import use_case.filter.WashroomFilterCriteria;
import use_case.login.Passwords;

/** Verifies deterministic persistence helpers without opening a database connection. */
class DatabaseUnitCoverageTest {
    @Test
    void conditionsAndBsonHelpersPreserveSchemaValues() {
        for (final Operator operator : List.of(Operator.EQ, Operator.NE, Operator.LT, Operator.GT, Operator.LTE,
            Operator.GTE)) {
            final Condition<Integer> condition = new Condition<>("score", operator, 3);
            assertEquals("score", condition.getFieldName());
            assertEquals(operator, condition.getOperator());
            assertEquals(3, condition.getValue());
            assertNotNull(condition.getFilter());
        }
        for (final Operator operator : List.of(Operator.IN, Operator.NIN)) {
            final CollectionCondition<List<String>> condition = new CollectionCondition<>("gender", operator,
                List.of("MEN", "WOMEN"));
            assertEquals(operator, condition.getOperator());
            assertNotNull(condition.getFilter());
        }

        final ObjectId id = new ObjectId();
        final Document document = new Document("_id", id)
            .append("first", " ")
            .append("second", "value")
            .append("numberText", "bad")
            .append("number", "4.5")
            .append("boolean", "true")
            .append("dateText", "2026-01-02T03:04:05");
        assertEquals(id.toHexString(), MongoDocuments.id(document));
        assertEquals("value", MongoDocuments.string(document, "fallback", "first", "second"));
        assertEquals(4.5, MongoDocuments.number(document, 1, "numberText", "number"));
        assertEquals(5, MongoDocuments.integer(document, 1, "number"));
        assertTrue(MongoDocuments.bool(document, false, "boolean"));
        assertEquals(LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            MongoDocuments.dateTime(document, LocalDateTime.MIN, "dateText"));
        final LocalDateTime directDateTime = LocalDateTime.of(2026, 2, 3, 4, 5);
        assertEquals(directDateTime,
            MongoDocuments.dateTime(new Document("direct", directDateTime), LocalDateTime.MIN, "direct"));
        assertEquals(LocalDate.of(2026, 1, 2), MongoDocuments.date(document, LocalDate.MIN, "dateText"));
        assertTrue(MongoDocuments.referenceMatches(id, id.toHexString()));
        assertTrue(MongoDocuments.referenceMatches("prefix-id", "id"));
        assertFalse(MongoDocuments.referenceMatches(null, "id"));
        assertFalse(MongoDocuments.referenceMatches("id", null));
        assertEquals("fallback", MongoDocuments.string(new Document(), "fallback", "missing"));
        assertEquals(7, MongoDocuments.integer(new Document(), 7, "missing"));
        assertFalse(MongoDocuments.bool(new Document(), false, "missing"));
        assertEquals(LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(id.getTimestamp()),
            java.time.ZoneId.systemDefault()), MongoDocuments.dateTime(new Document("_id", id), LocalDateTime.MIN));
    }

    @Test
    void inMemoryRepositoriesImplementTheirContracts() {
        final InMemoryReviewDataAccessObject reviews = new InMemoryReviewDataAccessObject(List.of());
        final Review review = new Review("r1", "w1", "alice", 4, 5, "good", 0, LocalDate.now());
        reviews.save(review);
        assertEquals(List.of(review), reviews.getReviewsForWashroom("w1"));
        assertEquals(List.of(review), reviews.getReviewsByUser("alice"));
        assertEquals(1, reviews.getSummary("w1").reviewCount());
        assertEquals(0, reviews.getSummary("missing").reviewCount());
        reviews.addVote("r1", "alice");
        assertTrue(reviews.hasVoted("r1", "alice"));
        assertEquals(Set.of("r1"), reviews.votedReviewIds(List.of("r1", "missing"), "alice"));
        reviews.addVote("r1", "alice");
        reviews.removeVote("r1", "alice");
        assertFalse(reviews.hasVoted("r1", "alice"));
        reviews.removeVote("r1", "alice");
        reviews.addVote("missing", "alice");
        reviews.removeVote("missing", "alice");
        final Report report = new Report("report", "r1", "alice", List.of("Spam"), "details", LocalDateTime.now());
        reviews.save(report);
        assertTrue(reviews.hasReported("r1", "alice"));
        assertEquals(Set.of("r1"), reviews.reportedReviewIds(List.of("r1"), "alice"));
        assertEquals(1, reviews.getAllReports().size());
        reviews.deleteReportsForReview("r1");
        reviews.deleteReview("r1");
        assertTrue(reviews.getById("r1").isEmpty());

        final InMemoryUserDataAccessObject users = new InMemoryUserDataAccessObject();
        assertTrue(users.existsByName("sheena_q"));
        assertTrue(users.get("missing").isEmpty());
        final User user = new User("alice", "hash", "", true);
        users.save(user);
        users.setCurrentUser(user);
        assertEquals(user, users.currentUser().orElseThrow());
        assertTrue(users.isModerator("alice"));
        users.clear();
        users.removeUser("alice");
        assertFalse(users.existsByName("alice"));

        final InMemoryWashroomDataAccessObject washrooms = new InMemoryWashroomDataAccessObject();
        assertEquals(3, washrooms.getAll().size());
        assertTrue(washrooms.getById("bahen-2").isPresent());
        assertTrue(washrooms.getById("missing").isEmpty());
        assertEquals(1, washrooms.getByIds(List.of("bahen-2")).size());
        assertTrue(washrooms.getNearby(43.6597, -79.3974, 1).contains(washrooms.getById("bahen-2").orElseThrow()));
        assertEquals(2, washrooms.findMatching(new WashroomFilterCriteria(true, List.of(Washroom.Gender.ALL_GENDER),
            null, Set.of())).size());
        assertEquals(1, washrooms.findMatching(new WashroomFilterCriteria(false, List.of(Washroom.Gender.WOMEN),
            "GE", Set.of())).size());

        final InMemoryStatusReportDataAccessObject statuses = new InMemoryStatusReportDataAccessObject();
        assertFalse(statuses.getRecentForWashroom("missing", LocalDateTime.MIN).iterator().hasNext());
        statuses.save(new StatusReport("w1", "alice", 3, 3, entity.MaintenanceIssue.NONE, LocalDateTime.now()));
        assertEquals(1, statuses.getForWashroom("w1", LocalDateTime.now().minusMinutes(1),
            LocalDateTime.now().plusMinutes(1)).size());
    }

    @Test
    void deterministicAdaptersExposeExpectedData(@TempDir final Path temp) throws Exception {
        final database.enrollment.TimetableEnrollmentDataAccessObject enrollment =
            new database.enrollment.TimetableEnrollmentDataAccessObject();
        assertEquals(3, enrollment.getBuildingSchedule("BA", DayOfWeek.MONDAY).size());
        assertEquals(2, enrollment.getBuildingSchedule("RB", DayOfWeek.MONDAY).size());
        assertEquals(2, enrollment.getBuildingSchedule("other", DayOfWeek.MONDAY).size());
        final Path calendar = temp.resolve("calendar.ics");
        Files.writeString(calendar, "BEGIN:VCALENDAR\nEND:VCALENDAR");
        assertTrue(new database.personal_plan.FileCalendarContentReader().read(calendar.toString()).contains("VCALENDAR"));

        final BcryptPasswordHasher hasher = new BcryptPasswordHasher();
        final String hash = hasher.hash("secret");
        assertTrue(hasher.matches("secret", hash));
        assertFalse(hasher.matches("wrong", hash));
        assertFalse(hasher.matches(null, hash));
        assertTrue(hasher.matches("secret", Passwords.hash("secret")));
        assertTrue(hasher.matches("same", "same"));
        assertFalse(hasher.isCurrentHash(null));
        assertTrue(hasher.isCurrentHash(hash));
    }
}
