package data_access.review;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import data_access.AbstractCondition;
import data_access.Condition;
import data_access.MongoDocuments;
import entity.review.WashroomReview;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import entity.review.Review;
import entity.review.WashroomReviewFactory;
import entity.washroom.Washroom;
import entity.Report;
import data_access.DBDataAccessObject;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;
import use_case.moderate_reviews.ReviewAdminDataAccessInterface;
import use_case.moderate_reviews.ReportedReviewsDataAccessInterface;
import use_case.report_review.ReviewReportDataAccessInterface;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.*;

public class DBReviewDataAccessObject extends DBDataAccessObject
        implements use_case.view_reviews.ReviewDataAccessInterface,
        HelpfulVoteDataAccessInterface, ReviewReportDataAccessInterface, ReviewAdminDataAccessInterface, ReportedReviewsDataAccessInterface {

    static MongoCollection<Document> collection;
    private static MongoCollection<Document> users;
    private final MongoCollection<Document> reviewVotes;
    private final MongoCollection<Document> reviewReports;
    static final List<String> allowedAttributes = List.of(new String[]{
            "washroomID", "authorUsername", "rating", "cleanliness", "comment", "helpfulCount",
            "createdAt", "seedKey"});

    public DBReviewDataAccessObject() {
        super();    // initializes the MongoClient and MongoDatabase from
        // the set URI
        collection = database.getCollection("Reviews");
        users = database.getCollection("Users");
        reviewVotes = database.getCollection("ReviewVotes");
        reviewReports = database.getCollection("ReviewReports");
    }

    public DBReviewDataAccessObject(MongoDatabase database) {
        super(database);
        collection = database.getCollection("Reviews");
        users = database.getCollection("Users");
        reviewVotes = database.getCollection("ReviewVotes");
        reviewReports = database.getCollection("ReviewReports");
    }

    /**
     * Returns all reviews who satisfy all the given conditions
     *
     * @param conditions a list of condition objects that the returned reviews must satisfy
     * @return The reviews that match all the conditions
     */
    public List<Review> getMatching(Iterable<AbstractCondition<?>> conditions) {

        return new ArrayList<>(getMatchingIDMap(conditions).values());

    }

    /**
     * Returns all reviews who satisfy the condition
     *
     * @param condition a condition object that the returned reviews must satisfy
     * @return The reviews that match the conditions
     */
    public List<Review> getMatching(AbstractCondition<?> condition) {

        List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        return getMatching(conditions);

    }

    /**
     * Returns all reviews which satisfy all the given conditions, with database IDs
     *
     * @param conditions a list of condition objects that the returned reviews must satisfy
     * @return The reviews that match all the conditions mapped to their IDs in the database.
     */
    public static Map<String, Review> getMatchingIDMap(Iterable<AbstractCondition<?>> conditions) {

        checkAttribute(conditions);

        Bson filter = parseConditions(conditions);
        List<Document> docs = getAll(filter);

        Map<String, Review> reviews = new HashMap<>();
        for (Document doc : docs) {
            Review review = createReview(doc);
            reviews.put(MongoDocuments.id(doc), review);
        }
        return reviews;

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
     * Creates a review object out of the inputted Document
     *
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
     *
     * @param review The Review object to be written.
     */
    public void write(Review review, String userID, String washroomID) {
        if (review instanceof entity.Review applicationReview) {
            collection.insertOne(documentFor(applicationReview));
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
     *
     * @param conditions List of AbstractCondition objects. An object must satisfy
     *                   all conditions to be deleted
     */
    public void delete(Iterable<AbstractCondition<?>> conditions) {
        checkAttribute(conditions);
        Bson filter = parseConditions(conditions);
        collection.deleteMany(filter);
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

    @Override
    public List<entity.Review> getReviewsForWashroom(String washroomId) {
        List<entity.Review> result = new ArrayList<>();
        for (Review review : getMatching(List.<AbstractCondition<?>>of())) {
            entity.Review applicationReview = (entity.Review) review;
            if (applicationReview.washroomId().equals(washroomId)) result.add(applicationReview);
        }
        return List.copyOf(result);
    }

    @Override
    public entity.ReviewSummary getSummary(String washroomId) {
        List<entity.Review> found = getReviewsForWashroom(washroomId);
        if (found.isEmpty()) return entity.ReviewSummary.empty();
        return new entity.ReviewSummary(found.stream().mapToDouble(entity.Review::rating).average().orElse(0),
                found.stream().mapToDouble(entity.Review::cleanliness).average().orElse(0), found.size());
    }

    @Override
    public List<entity.Review> getReviewsByUser(String username) {
        List<entity.Review> result = new ArrayList<>();
        for (Review legacyReview : getMatching(List.<AbstractCondition<?>>of())) {
            entity.Review review = (entity.Review) legacyReview;
            if (username.equals(review.authorUsername())) result.add(review);
        }
        return List.copyOf(result);
    }

    @Override public void save(entity.Review review) {
        write(review, review.authorUsername(), review.washroomId());
    }

    /** Adds the first-run demonstration reviews without duplicating them on later launches. */
    public void ensureCampusReviews(List<entity.Washroom> washrooms) {
        for (entity.Washroom washroom : washrooms) {
            List<SeedReview> seeds = seedReviewsFor(washroom);
            for (int index = 0; index < seeds.size(); index++) {
                String seedKey = "campus-review-" + washroom.building().code().toLowerCase(Locale.ROOT) + "-" + index;
                if (collection.find(Filters.eq("seedKey", seedKey)).first() != null) continue;
                SeedReview seed = seeds.get(index);
                entity.Review review = new entity.Review(seedKey, washroom.id(), seed.author(), seed.rating(),
                        seed.cleanliness(), seed.comment(), seed.helpfulCount(), seed.createdAt());
                collection.insertOne(documentFor(review).append("seedKey", seedKey));
            }
        }
    }

    private static String author(Document review) {
        String direct = MongoDocuments.string(review, "", "authorUsername", "username");
        if (!direct.isBlank()) return direct;
        String userId = MongoDocuments.string(review, "", "userId", "userID");
        Document user = MongoDocuments.findById(users, userId);
        return user == null ? "Anonymous" : MongoDocuments.string(user, "Anonymous", "username", "name");
    }

    private static Document documentFor(entity.Review review) {
        return new Document("washroomId", review.washroomId())
                .append("authorUsername", review.authorUsername())
                .append("rating", review.rating()).append("cleanliness", review.cleanliness())
                .append("comment", review.comment()).append("helpfulCount", review.helpfulCount())
                .append("createdAt", Date.from(review.createdAt().atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private static List<SeedReview> seedReviewsFor(entity.Washroom washroom) {
        return switch (washroom.building().code()) {
            case "BA" -> List.of(
                    new SeedReview("Maya", 5, 5, "Bright, spacious, and consistently well stocked between classes.", 12, LocalDate.of(2026, 5, 8)),
                    new SeedReview("Noah", 4, 4, "Easy to find on the main floor. It gets busy just after lectures.", 7, LocalDate.of(2026, 6, 14)));
            case "MY" -> List.of(
                    new SeedReview("Priya", 5, 4, "Very clean and the accessible stall is genuinely roomy.", 9, LocalDate.of(2026, 5, 22)),
                    new SeedReview("Liam", 4, 4, "A dependable option when studying in the engineering buildings.", 5, LocalDate.of(2026, 6, 3)));
            case "TC" -> List.of(
                    new SeedReview("Sofia", 4, 5, "Quiet, clean, and tucked away from the busiest campus routes.", 11, LocalDate.of(2026, 4, 29)),
                    new SeedReview("Ethan", 4, 4, "Good lighting and usually no wait in the morning.", 6, LocalDate.of(2026, 6, 10)));
            case "HH" -> List.of(
                    new SeedReview("Avery", 5, 5, "Well maintained and convenient when using Hart House amenities.", 10, LocalDate.of(2026, 5, 17)),
                    new SeedReview("Jordan", 4, 4, "Clean and calm, although the hallway can be crowded at lunch.", 4, LocalDate.of(2026, 6, 21)));
            default -> List.of(
                    new SeedReview("Campus visitor", 4, 4, "A clean and reliable washroom option.", 3, LocalDate.of(2026, 6, 1)));
        };
    }

    private static double clamp(double rating) {
        return Math.max(1, Math.min(5, rating));
    }

    // --- Helpful votes ---------------------------------------------------------
    @Override
    public boolean hasVoted(String reviewId, String username) {
        return reviewVotes.find(Filters.and(Filters.eq("reviewId", reviewId),
                Filters.eq("username", username))).first() != null;
    }

    @Override
    public void addVote(String reviewId, String username) {
        if (!hasVoted(reviewId, username)) {
            reviewVotes.insertOne(new Document("reviewId", reviewId).append("username", username));
            adjustHelpful(reviewId, 1);
        }
    }

    @Override
    public void removeVote(String reviewId, String username) {
        if (hasVoted(reviewId, username)) {
            reviewVotes.deleteOne(Filters.and(Filters.eq("reviewId", reviewId),
                    Filters.eq("username", username)));
            adjustHelpful(reviewId, -1);
        }
    }

    private void adjustHelpful(String reviewId, int delta) {
        Document document = MongoDocuments.findById(collection, reviewId);
        if (document != null) {
            collection.updateOne(Filters.eq("_id", document.get("_id")),
                    new Document("$inc", new Document("helpfulCount", delta)));
        }
    }

    // --- Reports ---------------------------------------------------------------
    @Override
    public void save(Report report) {
        reviewReports.insertOne(new Document("reviewId", report.reviewId())
                .append("reporterUsername", report.reporterUsername())
                .append("reasons", report.reasons())
                .append("details", report.details())
                .append("createdAt", report.createdAt().toString()));
    }

    @Override
    public boolean hasReported(String reviewId, String username) {
        return reviewReports.find(Filters.and(Filters.eq("reviewId", reviewId),
                Filters.eq("reporterUsername", username))).first() != null;
    }

    @Override
    public List<Report> getAllReports() {
        List<Report> result = new ArrayList<>();
        for (Document document : reviewReports.find()) {
            List<String> reasons = document.getList("reasons", String.class);
            result.add(new Report(MongoDocuments.id(document),
                    MongoDocuments.string(document, "", "reviewId"),
                    MongoDocuments.string(document, "Anonymous", "reporterUsername", "username"),
                    reasons == null ? List.of() : reasons,
                    MongoDocuments.string(document, "", "details"),
                    MongoDocuments.dateTime(document, LocalDateTime.now(), "createdAt")));
        }
        return List.copyOf(result);
    }

    @Override
    public void deleteReportsForReview(String reviewId) {
        reviewReports.deleteMany(Filters.eq("reviewId", reviewId));
    }

    // --- Review admin ----------------------------------------------------------
    @Override
    public Optional<entity.Review> getById(String reviewId) {
        Document document = MongoDocuments.findById(collection, reviewId);
        return document == null ? Optional.empty() : Optional.of((entity.Review) createReview(document));
    }

    @Override
    public void deleteReview(String reviewId) {
        Document document = MongoDocuments.findById(collection, reviewId);
        if (document != null) {
            collection.deleteOne(Filters.eq("_id", document.get("_id")));
        }
    }
    private record SeedReview(String author, double rating, double cleanliness, String comment, int helpfulCount,
                              LocalDate createdAt) { }
}
