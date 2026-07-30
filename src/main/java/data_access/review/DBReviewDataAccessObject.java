package data_access.review;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import data_access.Condition;
import data_access.MongoDocuments;
import entity.review.WashroomReview;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import entity.review.Review;
import entity.review.WashroomReviewFactory;
import entity.washroom.Washroom;
import data_access.DBDataAccessObject;

import java.time.LocalDate;
import java.util.*;

public class DBReviewDataAccessObject extends DBDataAccessObject implements use_case.view_reviews.ReviewDataAccessInterface {

    static MongoCollection<Document> collection;
    private static MongoCollection<Document> users;

    public DBReviewDataAccessObject() {
        super();    // initializes the MongoClient and MongoDatabase from
        // the set URI
        collection = database.getCollection("Reviews");
        users = database.getCollection("Users");
    }

    public DBReviewDataAccessObject(MongoDatabase database) {
        super(database);
        collection = database.getCollection("Reviews");
        users = database.getCollection("Users");
    }

    /**
     * Returns all reviews who satisfy all the given conditions
     * @param conditions a list of condition objects that the returned reviews must satisfy
     * @return The reviews that match all the conditions
     */
    public List<Review> getMatching(Iterable<Condition<?>> conditions) {

        Bson filter = parseConditions(conditions);
        List<Document> docs = getAll(filter);

        List<Review> reviews = new ArrayList<>();
        for (Document doc: docs) {
            Review review = createReview(doc);
            reviews.add(review);
        }
        return reviews;

    }

    /**
     * Parses a list of Condition objects into a single Bson filter
     * @param conditions list of condition objects to be connected by and statements
     * @return a Bson filter representing satisfying all conditions
     */
    private static Bson parseConditions(Iterable<Condition<?>> conditions) {
        List<Bson> filters = new ArrayList<>();
        conditions.forEach((condition) -> filters.add(condition.getFilter()));
        return filters.isEmpty() ? new Document() : Filters.and(filters);
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
     * Creates a review object out of the inputted Document
     * @param doc Document containing review data for a specific review
     * @return the review object constructed using that data
     */
    private static Review createReview(Document doc) {
        double rating = clamp(MongoDocuments.number(doc, 1, "rating", "stars"));
        double cleanliness = clamp(MongoDocuments.number(doc, rating, "cleanliness"));
        return new entity.Review(MongoDocuments.id(doc),
                MongoDocuments.string(doc, "unknown", "washroomId", "washroomID"),
                author(doc), rating, cleanliness,
                MongoDocuments.string(doc, "", "comment", "text"),
                Math.max(0, MongoDocuments.integer(doc, 0, "helpfulCount", "helpfuls")),
                MongoDocuments.date(doc, LocalDate.now(), "createdAt", "date"));
    }

    /**
     * Writes a single Review object to the database.
     * @param review The Review object to be written.
     */
    public void write(Review review, String userID, String washroomID) {
        if (review instanceof entity.Review applicationReview) {
            Document doc = new Document("washroomId", applicationReview.washroomId())
                    .append("authorUsername", applicationReview.authorUsername())
                    .append("rating", applicationReview.rating()).append("cleanliness", applicationReview.cleanliness())
                    .append("comment", applicationReview.comment()).append("helpfulCount", applicationReview.helpfulCount())
                    .append("createdAt", applicationReview.createdAt().toString());
            collection.insertOne(doc);
            return;
        }
        Document doc = new Document();
        doc.append("userID", userID);
        doc.append("washroomID", washroomID);
        doc.append("stars", review.getStars());
        doc.append("text", review.getText());
        doc.append("helpfuls", review.getHelpfuls());
        doc.append("unhelpfuls", review.getUnhelpfuls());

        collection.insertOne(doc);
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

    @Override public List<entity.Review> getReviewsForWashroom(String washroomId) {
        List<entity.Review> result = new ArrayList<>();
        for (Review review : getMatching(List.<Condition<?>>of())) {
            entity.Review applicationReview = (entity.Review) review;
            if (applicationReview.washroomId().equals(washroomId)) result.add(applicationReview);
        }
        return List.copyOf(result);
    }

    @Override public entity.ReviewSummary getSummary(String washroomId) {
        List<entity.Review> found = getReviewsForWashroom(washroomId);
        if (found.isEmpty()) return entity.ReviewSummary.empty();
        return new entity.ReviewSummary(found.stream().mapToDouble(entity.Review::rating).average().orElse(0),
                found.stream().mapToDouble(entity.Review::cleanliness).average().orElse(0), found.size());
    }

    @Override public List<entity.Review> getReviewsByUser(String username) {
        List<entity.Review> result = new ArrayList<>();
        for (Review legacyReview : getMatching(List.<Condition<?>>of())) {
            entity.Review review = (entity.Review) legacyReview;
            if (username.equals(review.authorUsername())) result.add(review);
        }
        return List.copyOf(result);
    }

    public void save(entity.Review review) {
        write(review, review.authorUsername(), review.washroomId());
    }

    private static String author(Document review) {
        String direct = MongoDocuments.string(review, "", "authorUsername", "username");
        if (!direct.isBlank()) return direct;
        String userId = MongoDocuments.string(review, "", "userId", "userID");
        Document user = MongoDocuments.findById(users, userId);
        return user == null ? "Anonymous" : MongoDocuments.string(user, "Anonymous", "username", "name");
    }

    private static double clamp(double rating) { return Math.max(1, Math.min(5, rating)); }
}
