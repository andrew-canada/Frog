package database.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import database.MongoDocuments;
import entity.MaintenanceIssue;
import entity.StatusReport;
import use_case.port.StatusReportRepository;

/**
 * Status reports are represented in the database as follows:
 *  - _id: MongoDB required attribute, an ObjectID representing the unique object.
 *  - washroomId: String, the _id field of the washroom the status report is for.
 *  - username: String or null, the username of the user who made the report.
 *  - busyness: Integer 1-5 representing busyness.
 *  - cleanliness: Integer 1-5 representing cleanliness.
 *  - issue: String in all caps such as "NONE", "OUT_OF_SOUP" corresponding to a MaintenanceIssue.
 *  - timestamp: Datetime when the report was made.
 *  - hourOfDay: Integer, hour in 24 hour time.
 */

public final class DBStatusReportDataAccessObject implements StatusReportRepository {
    private final MongoCollection<Document> reports;

    public DBStatusReportDataAccessObject(final MongoDatabase database) {
        reports = database.getCollection("StatusReports");
    }

    private static int clamp(final int value) {
        return Math.max(1, Math.min(5, value));
    }

    @Override
    public void save(final StatusReport report) {
        reports.insertOne(new Document("washroomId", report.washroomId())
            .append("username", report.username())
            .append("busyness", report.busyness())
            .append("cleanliness", report.cleanliness())
            .append("issue", report
                .issue()
                .name())
            .append("hourOfDay", report
                .timestamp()
                .getHour())
            .append("timestamp", Date.from(report
                .timestamp()
                .atZone(ZoneId.systemDefault())
                .toInstant())));
    }

    /**
     * Adds one persistent, varied status report for every hour of every JSON-sourced washroom.
     */
    public void ensureJsonHourlyReports(final List<entity.Washroom> washrooms) {
        final LocalDate reportDay = LocalDate
            .now()
            .minusDays(1);
        final Set<String> existingSeedKeys = new HashSet<>();
        for (final Document report : reports.find(Filters.regex("seedKey", "^json-hourly-status-"))) {
            existingSeedKeys.add(MongoDocuments.string(report, "", "seedKey"));
        }
        final List<Document> newReports = new ArrayList<>();
        for (int washroomIndex = 0; washroomIndex < washrooms.size(); washroomIndex++) {
            final entity.Washroom washroom = washrooms.get(washroomIndex);
            for (int hour = 0; hour < 24; hour++) {
                final String seedKey = "json-hourly-status-" + washroom.id() + "-" + hour;
                if (existingSeedKeys.contains(seedKey)) {
                    continue;
                }
                final int busyness = 1 + Math.floorMod(washroomIndex * 2 + hour * 3, 5);
                final int cleanliness = 1 + Math.floorMod(washroomIndex * 3 + hour * 2, 5);
                final LocalDateTime timestamp = LocalDateTime.of(reportDay, LocalTime.of(hour, 0));
                newReports.add(new Document("washroomId", washroom.id())
                    .append("username", "System seed")
                    .append("busyness", busyness)
                    .append("cleanliness", cleanliness)
                    .append("issue", MaintenanceIssue.NONE.name())
                    .append("hourOfDay", hour)
                    .append("timestamp", Date.from(timestamp
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                    .append("seedKey", seedKey));
            }
        }
        if (!newReports.isEmpty()) {
            reports.insertMany(newReports);
        }
    }

    @Override
    public List<StatusReport> getRecentForWashroom(final String washroomId, final LocalDateTime since) {
        return getForWashroom(washroomId, since, LocalDateTime
            .now()
            .plusSeconds(1));
    }

    @Override
    public List<StatusReport> getForWashroom(final String washroomId, final LocalDateTime from,
                                             final LocalDateTime to) {
        final List<StatusReport> result = new ArrayList<>();
        for (final Document document : reports.find()) {
            if (!MongoDocuments.referenceMatches(document.get("washroomId"), washroomId)
                && !MongoDocuments.referenceMatches(document.get("washroomID"), washroomId)) {
                continue;
            }
            final StatusReport report = toEntity(document);
            if (!report
                .timestamp()
                .isBefore(from) && !report
                .timestamp()
                .isAfter(to)) {
                result.add(report);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Returns each requested washroom's newest report in one current-hour aggregation.
     */
    @Override
    public Map<String, StatusReport> getCurrentHourForWashrooms(final List<String> washroomIds, final int hour) {
        if (washroomIds.isEmpty()) {
            return Map.of();
        }
        final Document currentHour = new Document("$or", List.of(new Document("hourOfDay", hour),
            new Document("$expr", new Document("$eq", List.of(new Document("$hour", "$timestamp"), hour)))));
        final List<Document> pipeline = List.of(new Document("$match",
                new Document("washroomId", new Document("$in", washroomIds)).append("$or", currentHour.get("$or"))),
            new Document("$sort", new Document("timestamp", -1)), new Document("$group",
                new Document("_id", "$washroomId").append("latest", new Document("$first", "$$ROOT"))),
            new Document("$replaceRoot", new Document("newRoot", "$latest")));
        final Map<String, StatusReport> result = new HashMap<>();
        for (final Document document : reports.aggregate(pipeline)) {
            final StatusReport report = toEntity(document);
            result.put(report.washroomId(), report);
        }
        return Map.copyOf(result);
    }

    /**
     * Backfills the hour bucket once, then indexes the current-hour heatmap query.
     */
    public void ensurePerformanceIndexes() {
        reports.updateMany(Filters.exists("hourOfDay", false),
            List.of(new Document("$set", new Document("hourOfDay", new Document("$hour", "$timestamp")))));
        reports.createIndex(Indexes.compoundIndex(Indexes.ascending("hourOfDay"), Indexes.ascending("washroomId"),
            Indexes.descending("timestamp")));
    }

    private StatusReport toEntity(final Document document) {
        final String issueName =
            MongoDocuments.string(document, MaintenanceIssue.NONE.name(), "issue", "maintenanceIssue");
        MaintenanceIssue issue;
        try {
            issue = MaintenanceIssue.valueOf(issueName);
        }
        catch (final IllegalArgumentException ignored) {
            issue = MaintenanceIssue.OTHER;
        }
        return new StatusReport(MongoDocuments.string(document, "unknown", "washroomId", "washroomID"),
            MongoDocuments.string(document, null, "username"), clamp(MongoDocuments.integer(document, 1, "busyness")),
            clamp(MongoDocuments.integer(document, 1, "cleanliness")), issue,
            MongoDocuments.dateTime(document, LocalDateTime.now(), "timestamp", "createdAt"));
    }
}
