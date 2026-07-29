package data_access.review;

import com.mongodb.client.model.Filters;
import data_access.Condition;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import entity.review.Review;
import entity.review.WashroomReviewFactory;
import entity.washroom.Washroom;
import data_access.DBDataAccessObject;

import java.util.*;

public class DBReviewDataAccessObject extends DBDataAccessObject {

    static MongoCollection<Document> collection;

    public DBReviewDataAccessObject() {
        super();    // initializes the MongoClient and MongoDatabase from
        // the set URI
        collection = database.getCollection("Reviews");
    }

    /**
     * Returns all reviews who satisfy all the given conditions
     * @param conditions a list of condition objects that the returned reviews must satisfy
     * @return The reviews that match all the conditions
     */
    public static List<Review> getMatching(Iterable<Condition<?>> conditions) {

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
     * Creates a review object out of the inputted Document
     * @param doc Document containing review data for a specific review
     * @return the review object constructed using that data
     */
    private static Review createReview(Document doc) {
        Review review = WashroomReviewFactory.create(
                doc.getInteger("stars"),
                doc.getString("text"),
                doc.getInteger("helpfuls"),
                doc.getInteger("unhelpfuls"));
        return review;
    }

    /**
     * Writes a single Review object to the database.
     * @param review The Review object to be written.
     */
    public void write(Review review, String userID, String washroomID) {
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
}
