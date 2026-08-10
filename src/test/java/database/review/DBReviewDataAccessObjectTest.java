package database.review;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bson.BsonDocument;
import org.bson.Document;

import com.mongodb.MongoClientSettings;

/** Verifies compatibility with both persisted review schemas. */
final class DBReviewDataAccessObjectTest {
    @org.junit.jupiter.api.Test
    void findsReviewsWrittenWithEitherWashroomIdField() {
        final BsonDocument filter = DBReviewDataAccessObject
            .washroomIdFilter("washroom-1")
            .toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry());
        final String query = filter.toJson();

        assertTrue(query.contains("washroomID"));
        assertTrue(query.contains("washroomId"));
    }
}
