package data_access.review;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import data_access.MongoDocuments;
import entity.Review;
import entity.ReviewSummary;
import org.bson.Document;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class MongoReviewDataAccessObject implements use_case.view_reviews.ReviewDataAccessInterface {
    private final MongoCollection<Document> reviews;
    private final MongoCollection<Document> users;

    public MongoReviewDataAccessObject(MongoDatabase database) {
        reviews = database.getCollection("Reviews");
        users = database.getCollection("Users");
    }

    @Override public List<Review> getReviewsForWashroom(String washroomId) {
        List<Review> result = new ArrayList<>();
        for (Document document : reviews.find()) {
            if (matchesWashroom(document, washroomId)) result.add(toEntity(document));
        }
        return List.copyOf(result);
    }

    @Override public ReviewSummary getSummary(String washroomId) {
        List<Review> found = getReviewsForWashroom(washroomId);
        if (found.isEmpty()) return ReviewSummary.empty();
        return new ReviewSummary(found.stream().mapToDouble(Review::rating).average().orElse(0),
                found.stream().mapToDouble(Review::cleanliness).average().orElse(0), found.size());
    }

    @Override public List<Review> getReviewsByUser(String username) {
        return reviews.find().into(new ArrayList<>()).stream().map(this::toEntity)
                .filter(review -> username.equals(review.authorUsername())).toList();
    }

    public void save(Review review) {
        reviews.insertOne(new Document("washroomId", review.washroomId())
                .append("authorUsername", review.authorUsername())
                .append("rating", review.rating()).append("cleanliness", review.cleanliness())
                .append("comment", review.comment()).append("helpfulCount", review.helpfulCount())
                .append("createdAt", review.createdAt().toString()));
    }

    private boolean matchesWashroom(Document document, String washroomId) {
        return MongoDocuments.referenceMatches(document.get("washroomId"), washroomId)
                || MongoDocuments.referenceMatches(document.get("washroomID"), washroomId);
    }

    private Review toEntity(Document document) {
        double rating = clamp(MongoDocuments.number(document, 1, "rating", "stars"));
        double cleanliness = clamp(MongoDocuments.number(document, rating, "cleanliness"));
        return new Review(MongoDocuments.id(document),
                MongoDocuments.string(document, "unknown", "washroomId", "washroomID"),
                author(document), rating, cleanliness,
                MongoDocuments.string(document, "", "comment", "text"),
                Math.max(0, MongoDocuments.integer(document, 0, "helpfulCount", "helpfuls")),
                MongoDocuments.date(document, LocalDate.now(), "createdAt", "date"));
    }

    private String author(Document review) {
        String direct = MongoDocuments.string(review, "", "authorUsername", "username");
        if (!direct.isBlank()) return direct;
        String userId = MongoDocuments.string(review, "", "userId", "userID");
        Document user = MongoDocuments.findById(users, userId);
        return user == null ? "Anonymous" : MongoDocuments.string(user, "Anonymous", "username", "name");
    }

    private static double clamp(double rating) { return Math.max(1, Math.min(5, rating)); }
}

