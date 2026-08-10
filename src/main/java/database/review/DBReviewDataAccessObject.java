package database.review;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import database.AbstractCondition;
import database.DBDataAccessObject;
import database.MongoDocuments;
import entity.Report;
import entity.Review;
import use_case.moderate_reviews.ReportedReviewsDataAccessInterface;
import use_case.moderate_reviews.ReviewAdminDataAccessInterface;
import use_case.port.ReviewRepository;
import use_case.report_review.ReviewReportDataAccessInterface;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;

public class DBReviewDataAccessObject extends DBDataAccessObject
    implements ReviewRepository, HelpfulVoteDataAccessInterface, ReviewReportDataAccessInterface,
    ReviewAdminDataAccessInterface, ReportedReviewsDataAccessInterface {
    /** Field used by existing review documents. */
    private static final String FIELD_WASHROOM_ID_LEGACY = "washroomId";
    /** Field used when this application persists a review. */
    private static final String FIELD_WASHROOM_ID = "washroomID";
    private static final String FIELD_AUTHORUSERNAME = "authorUsername";
    private static final String FIELD_RATING = "rating";
    private static final String FIELD_CLEANLINESS = "cleanliness";
    private static final String FIELD_COMMENT = "comment";
    private static final String FIELD_HELPFULCOUNT = "helpfulCount";
    private static final String FIELD_CREATEDAT = "createdAt";
    private static final String FIELD_SEEDKEY = "seedKey";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_ANONYMOUS = "Anonymous";
    private static final String FIELD_REVIEWID = "reviewId";
    private static final String FIELD_REPORTERUSERNAME = "reporterUsername";
    private static final String FIELD_ID = "_id";
    private static final String FIELD_DETAILS = "details";
    private static final int MAX_RATING_LEVEL = 5;
    private static final int SEED_REVIEW_MONTH = 7;
    private static final int SEED_REVIEW_YEAR = 2026;
    private static final int SEED_HELPFUL_COUNT_MODULUS = 9;
    private static final int SEED_REVIEW_INDEX_FACTOR = 3;
    private static final int HIGH_RATING_THRESHOLD = 4;
    private static final double LOW_RATING_THRESHOLD = 1.5;

    private static final List<String> ALLOWED_ATTRIBUTES = List.of(
        new String[] {FIELD_WASHROOM_ID, FIELD_AUTHORUSERNAME, FIELD_RATING, FIELD_CLEANLINESS, FIELD_COMMENT,
            FIELD_HELPFULCOUNT, FIELD_CREATEDAT,
            FIELD_SEEDKEY});
    private final MongoCollection<Document> collection;
    private final MongoCollection<Document> users;
    private final MongoCollection<Document> reviewVotes;
    private final MongoCollection<Document> reviewReports;

    public DBReviewDataAccessObject() {
        collection = database().getCollection("Reviews");
        users = database().getCollection("Users");
        reviewVotes = database().getCollection("ReviewVotes");
        reviewReports = database().getCollection("ReviewReports");
    }

    public DBReviewDataAccessObject(final MongoDatabase database) {
        super(database);
        collection = database.getCollection("Reviews");
        users = database.getCollection("Users");
        reviewVotes = database.getCollection("ReviewVotes");
        reviewReports = database.getCollection("ReviewReports");
    }

    /**
     * Returns all reviews which satisfy all the given conditions, with database IDs.
     *
     * @param conditions a list of condition objects that the returned reviews must satisfy
     * @return The reviews that match all the conditions mapped to their IDs in the database.
     */
    private Map<String, Review> getMatchingIDMap(final Iterable<AbstractCondition<?>> conditions) {

        checkAttribute(conditions);

        final Bson filter = parseConditions(conditions);
        final List<Document> docs = getAll(filter);

        final Map<String, Review> reviews = new HashMap<>();
        for (final Document doc : docs) {
            final Review review = createReview(doc);
            reviews.put(MongoDocuments.id(doc), review);
        }
        return reviews;

    }

    /**
     * Parses a list of AbstractCondition objects into a single Bson filter.
     *
     * @param conditions list of condition objects to be connected by and statements
     * @return a Bson filter representing satisfying all conditions
     */
    private static Bson parseConditions(final Iterable<AbstractCondition<?>> conditions) {
        final List<Bson> filters = new ArrayList<>();
        conditions.forEach(condition -> {
            filters.add(condition.getFilter());
        });
        final Bson result;
        if (filters.isEmpty()) {
            result = new Document();
        }
        else {
            result = Filters.and(filters);
        }
        return result;
    }

    /**
     * Return a list of Documents which match the specified parameters.
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
     * Creates a review object out of the inputted Document.
     *
     * @param doc Document containing review data for a specific review
     * @return the review object constructed using that data
     */
    private Review createReview(final Document doc) {
        final double rating = clamp(MongoDocuments.number(doc, 1, FIELD_RATING, "stars"));
        final double cleanliness = clamp(MongoDocuments.number(doc, rating, FIELD_CLEANLINESS));
        return new entity.Review(MongoDocuments.id(doc),
            MongoDocuments.string(doc, "unknown", FIELD_WASHROOM_ID, FIELD_WASHROOM_ID_LEGACY), author(doc),
            rating, cleanliness,
            MongoDocuments.string(doc, "", FIELD_COMMENT, "text"),
            Math.max(0, MongoDocuments.integer(doc, 0, FIELD_HELPFULCOUNT, "helpfuls")),
            MongoDocuments.date(doc, LocalDate.now(), FIELD_CREATEDAT, "date"));
    }

    /**
     * Checks the condition against the list of allowed attributes, throwing a
     * runtime exception if it's not a valid attribute.
     *
     * @param condition The condition to check.
     * @throws RuntimeException if the attribute is not allowed.
     */
    private static void checkAttribute(final AbstractCondition<?> condition) {
        if (!ALLOWED_ATTRIBUTES.contains(condition.getFieldName())) {
            throw new RuntimeException("Not a valid attribute");
        }
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

    private String author(final Document review) {
        final String direct = MongoDocuments.string(review, "", FIELD_AUTHORUSERNAME, FIELD_USERNAME);
        final String result;
        if (!direct.isBlank()) {
            result = direct;
        }
        else {
            final String userId = MongoDocuments.string(review, "", "userId", "userID");
            final Document user = MongoDocuments.findById(users, userId);
            if (user == null) {
                result = FIELD_ANONYMOUS;
            }
            else {
                result = MongoDocuments.string(user, FIELD_ANONYMOUS, FIELD_USERNAME, "name");
            }
        }
        return result;
    }

    private static Document documentFor(final entity.Review review) {
        return new Document(FIELD_WASHROOM_ID, review.washroomId())
            .append(FIELD_AUTHORUSERNAME, review.authorUsername())
            .append(FIELD_RATING, review.rating())
            .append(FIELD_CLEANLINESS, review.cleanliness())
            .append(FIELD_COMMENT, review.comment())
            .append(FIELD_HELPFULCOUNT, review.helpfulCount())
            .append(FIELD_CREATEDAT, Date.from(review
                .createdAt()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()));
    }

    private static SeedReview seedReviewFor(final int washroomIndex, final int reviewIndex) {
        final double rating = 1 + .5 * Math.floorMod(washroomIndex * 2 + reviewIndex * 3, 9);
        final double cleanliness = 1 + .5 * Math.floorMod(washroomIndex * 3 + reviewIndex * 2, 9);
        final String comment;
        if (rating <= LOW_RATING_THRESHOLD) {
            comment = "The supplies were low and the space needed more attention during this visit.";
        }
        else {
            if (rating <= SEED_REVIEW_INDEX_FACTOR) {
                comment = "It was usable, but cleanliness and availability could be more consistent.";
            }
            else {
                if (rating <= HIGH_RATING_THRESHOLD) {
                    comment = "A solid option that was easy to find and reasonably well maintained.";
                }
                else {
                    comment = "Clean, well stocked, and convenient for a quick stop between classes.";
                }
            }
        }
        final String author;
        if (reviewIndex == 0) {
            author = "Campus visitor";
        }
        else {
            author = "Student reviewer";
        }
        return new SeedReview(author, rating, cleanliness, comment, Math.floorMod(washroomIndex
            + reviewIndex * SEED_REVIEW_INDEX_FACTOR, SEED_HELPFUL_COUNT_MODULUS),
            LocalDate
                .of(SEED_REVIEW_YEAR, SEED_REVIEW_MONTH, 1)
                .minusDays(washroomIndex * 2L + reviewIndex));
    }

    private static double clamp(final double rating) {
        return Math.max(1, Math.min(MAX_RATING_LEVEL, rating));
    }

    /**
     * Returns all reviews who satisfy all the given conditions.
     *
     * @param conditions a list of condition objects that the returned reviews must satisfy
     * @return The reviews that match all the conditions
     */
    private List<Review> getMatchingConditions(final Iterable<AbstractCondition<?>> conditions) {

        return new ArrayList<>(getMatchingIDMap(conditions).values());

    }

    /**
     * Returns all reviews who satisfy the condition.
     *
     * @param condition a condition object that the returned reviews must satisfy
     * @return The reviews that match the conditions
     */
    public List<Review> getMatching(final AbstractCondition<?> condition) {

        final List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        return getMatchingConditions(conditions);

    }

    /**
     * Writes a single Review object to the database.
     *
     * @param review The Review object to be written.
     * @param userID parameter value.
     * @param washroomID parameter value.
     */
    private void write(final Review review, final String userID, final String washroomID) {
        final Document doc;
        if (review instanceof final entity.Review applicationReview) {
            doc = documentFor(applicationReview);
        }
        else {
            doc = new Document();
            doc.append("userID", userID);
            doc.append(FIELD_WASHROOM_ID, washroomID);
            doc.append("stars", review.getStars());
            doc.append("text", review.getText());
            doc.append("helpfuls", review.getHelpfuls());
            doc.append("unhelpfuls", review.getUnhelpfuls());
        }
        collection.insertOne(doc);
    }

    /**
     * Deletes every entry in the database that matches the given conditions.
     *
     * @param conditions List of AbstractCondition objects. An object must satisfy
     *                   all conditions to be deleted
     */
    private void delete(final Iterable<AbstractCondition<?>> conditions) {
        checkAttribute(conditions);
        final Bson filter = parseConditions(conditions);
        collection.deleteMany(filter);
    }

    /**
     * Deletes every entry in the database that matches the given condition.
     *
     * @param condition A AbstractCondition object that the object must satisfy.
     */
    public void delete(final AbstractCondition<?> condition) {
        final List<AbstractCondition<?>> conditions = new ArrayList<>();
        conditions.add(condition);
        delete(conditions);
    }

    @Override
    public List<entity.Review> getReviewsForWashroom(final String washroomId) {
        final List<entity.Review> result = new ArrayList<>();
        for (final Document document : collection.find(washroomIdFilter(washroomId))) {
            result.add(createReview(document));
        }
        return List.copyOf(result);
    }

    static Bson washroomIdFilter(final String washroomId) {
        return Filters.or(Filters.eq(FIELD_WASHROOM_ID, washroomId),
            Filters.eq(FIELD_WASHROOM_ID_LEGACY, washroomId));
    }

    @Override
    public entity.ReviewSummary getSummary(final String washroomId) {
        final List<entity.Review> found = getReviewsForWashroom(washroomId);
        final entity.ReviewSummary result;
        if (found.isEmpty()) {
            result = entity.ReviewSummary.empty();
        }
        else {
            result = new entity.ReviewSummary(found
                .stream()
                .mapToDouble(entity.Review::rating)
                .average()
                .orElse(0), found
                .stream()
                .mapToDouble(entity.Review::cleanliness)
                .average()
                .orElse(0), found.size());
        }
        return result;
    }

    @Override
    public List<entity.Review> getReviewsByUser(final String username) {
        final List<entity.Review> result = new ArrayList<>();
        for (final Document document : collection.find(
            Filters.or(Filters.eq(FIELD_AUTHORUSERNAME, username), Filters.eq(FIELD_USERNAME, username)))) {
            result.add(createReview(document));
        }
        return List.copyOf(result);
    }

    @Override
    public void save(final entity.Review review) {
        write(review, review.authorUsername(), review.washroomId());
    }

    @Override
    public void save(final Report report) {
        reviewReports.insertOne(new Document(FIELD_REVIEWID, report.reviewId())
            .append(FIELD_REPORTERUSERNAME, report.reporterUsername())
            .append("reasons", report.reasons())
            .append(FIELD_DETAILS, report.details())
            .append(FIELD_CREATEDAT, report
                .createdAt()
                .toString()));
    }

    /**
     * Adds two persistent written reviews to every JSON-sourced washroom without duplicating them.
     * @param washrooms parameter value.
     */
    public void ensureJsonReviews(final List<entity.Washroom> washrooms) {
        final Set<String> existingSeedKeys = new HashSet<>();
        for (final Document review : collection.find(Filters.regex(FIELD_SEEDKEY, "^json-review-"))) {
            existingSeedKeys.add(MongoDocuments.string(review, "", FIELD_SEEDKEY));
        }
        final List<Document> newReviews = new ArrayList<>();
        for (int washroomIndex = 0; washroomIndex < washrooms.size(); washroomIndex++) {
            final entity.Washroom washroom = washrooms.get(washroomIndex);
            for (int index = 0; index < 2; index++) {
                final String seedKey = "json-review-" + washroom.id() + "-" + index;
                if (existingSeedKeys.contains(seedKey)) {
                    continue;
                }
                final SeedReview seed = seedReviewFor(washroomIndex, index);
                final entity.Review review =
                    new entity.Review(seedKey, washroom.id(), seed.author(), seed.rating(), seed.cleanliness(),
                        seed.comment(), seed.helpfulCount(), seed.createdAt());
                newReviews.add(documentFor(review).append(FIELD_SEEDKEY, seedKey));
            }
        }
        if (!newReviews.isEmpty()) {
            collection.insertMany(newReviews);
        }
    }

    /**
     * Creates indexes used by grouped summaries and the "my reviews" filter.
     */
    public void ensurePerformanceIndexes() {
        collection.createIndex(Indexes.ascending(FIELD_WASHROOM_ID));
        collection.createIndex(Indexes.ascending(FIELD_WASHROOM_ID_LEGACY));
        collection.createIndex(Indexes.ascending(FIELD_AUTHORUSERNAME));
        reviewVotes.createIndex(Indexes.compoundIndex(Indexes.ascending(FIELD_USERNAME),
            Indexes.ascending(FIELD_REVIEWID)));
        reviewReports.createIndex(
            Indexes.compoundIndex(Indexes.ascending(FIELD_REPORTERUSERNAME), Indexes.ascending(FIELD_REVIEWID)));
    }

    // --- Helpful votes ---------------------------------------------------------

    @Override
    public boolean hasVoted(final String reviewId, final String username) {
        return reviewVotes
            .find(Filters.and(Filters.eq(FIELD_REVIEWID, reviewId), Filters.eq(FIELD_USERNAME, username)))
            .first() != null;
    }

    @Override
    public Set<String> votedReviewIds(final Collection<String> reviewIds, final String username) {
        final Set<String> result;
        if (reviewIds.isEmpty() || username == null || username.isBlank()) {
            result = Set.of();
        }
        else {
            final Set<String> votedIds = new HashSet<>();
            for (final Document vote : reviewVotes.find(
                Filters.and(Filters.in(FIELD_REVIEWID, reviewIds), Filters.eq(FIELD_USERNAME, username)))) {
                votedIds.add(MongoDocuments.string(vote, "", FIELD_REVIEWID));
            }
            result = Set.copyOf(votedIds);
        }
        return result;
    }

    @Override
    public void addVote(final String reviewId, final String username) {
        if (!hasVoted(reviewId, username)) {
            reviewVotes.insertOne(new Document(FIELD_REVIEWID, reviewId).append(FIELD_USERNAME, username));
            adjustHelpful(reviewId, 1);
        }
    }

    @Override
    public void removeVote(final String reviewId, final String username) {
        if (hasVoted(reviewId, username)) {
            reviewVotes.deleteOne(Filters.and(Filters.eq(FIELD_REVIEWID, reviewId), Filters.eq(FIELD_USERNAME,
                username)));
            adjustHelpful(reviewId, -1);
        }
    }

    private void adjustHelpful(final String reviewId, final int delta) {
        final Document document = MongoDocuments.findById(collection, reviewId);
        if (document != null) {
            collection.updateOne(Filters.eq(FIELD_ID, document.get(FIELD_ID)),
                new Document("$inc", new Document(FIELD_HELPFULCOUNT, delta)));
        }
    }

    // --- Reports ---------------------------------------------------------------

    @Override
    public boolean hasReported(final String reviewId, final String username) {
        return reviewReports
            .find(Filters.and(Filters.eq(FIELD_REVIEWID, reviewId), Filters.eq(FIELD_REPORTERUSERNAME, username)))
            .first() != null;
    }

    @Override
    public Set<String> reportedReviewIds(final Collection<String> reviewIds, final String username) {
        final Set<String> result;
        if (reviewIds.isEmpty() || username == null || username.isBlank()) {
            result = Set.of();
        }
        else {
            final Set<String> reportedIds = new HashSet<>();
            for (final Document report : reviewReports.find(
                Filters.and(Filters.in(FIELD_REVIEWID, reviewIds), Filters.eq(FIELD_REPORTERUSERNAME, username)))) {
                reportedIds.add(MongoDocuments.string(report, "", FIELD_REVIEWID));
            }
            result = Set.copyOf(reportedIds);
        }
        return result;
    }

    @Override
    public List<Report> getAllReports() {
        final List<Report> result = new ArrayList<>();
        for (final Document document : reviewReports.find()) {
            final List<String> reasons = document.getList("reasons", String.class);
            if (reasons == null) {
                result.add(new Report(MongoDocuments.id(document), MongoDocuments.string(document, "", FIELD_REVIEWID),
                    MongoDocuments.string(document, FIELD_ANONYMOUS, FIELD_REPORTERUSERNAME, FIELD_USERNAME), List.of(),
                    MongoDocuments.string(document, "", FIELD_DETAILS),
                    MongoDocuments.dateTime(document, LocalDateTime.now(), FIELD_CREATEDAT)));
            }
            else {
                result.add(new Report(MongoDocuments.id(document), MongoDocuments.string(document, "", FIELD_REVIEWID),
                    MongoDocuments.string(document, FIELD_ANONYMOUS, FIELD_REPORTERUSERNAME, FIELD_USERNAME), reasons,
                    MongoDocuments.string(document, "", FIELD_DETAILS),
                    MongoDocuments.dateTime(document, LocalDateTime.now(), FIELD_CREATEDAT)));
            }
        }
        return List.copyOf(result);
    }

    @Override
    public void deleteReportsForReview(final String reviewId) {
        reviewReports.deleteMany(Filters.eq(FIELD_REVIEWID, reviewId));
    }

    // --- Review admin ----------------------------------------------------------

    @Override
    public Optional<entity.Review> getById(final String reviewId) {
        final Document document = MongoDocuments.findById(collection, reviewId);
        final Optional<entity.Review> result;
        if (document == null) {
            result = Optional.empty();
        }
        else {
            result = Optional.of(createReview(document));
        }
        return result;
    }

    @Override
    public void deleteReview(final String reviewId) {
        final Document document = MongoDocuments.findById(collection, reviewId);
        if (document != null) {
            collection.deleteOne(Filters.eq(FIELD_ID, document.get(FIELD_ID)));
        }
    }

    private record SeedReview(String author, double rating, double cleanliness, String comment, int helpfulCount,
                              LocalDate createdAt) {
    }
}
