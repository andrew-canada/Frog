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

public final class DBStatusReportDataAccessObject implements StatusReportRepository {
    private static final String FIELD_WASHROOMID = "washroomId";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_BUSYNESS = "busyness";
    private static final String FIELD_CLEANLINESS = "cleanliness";
    private static final String FIELD_ISSUE = "issue";
    private static final String FIELD_HOUROFDAY = "hourOfDay";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_SEEDKEY = "seedKey";
    private static final String FIELD__OR = "$or";
    private static final int MAGIC_24 = 24;
    private static final int MAGIC_5 = 5;
    private final MongoCollection<Document> reports;

    public DBStatusReportDataAccessObject(final MongoDatabase database) {
        reports = database.getCollection("StatusReports");
    }

    private static int clamp(final int value) {
        return Math.max(1, Math.min(MAGIC_5, value));
    }

    @Override
    public void save(final StatusReport report) {
        reports.insertOne(new Document(FIELD_WASHROOMID, report.washroomId())
            .append(FIELD_USERNAME, report.username())
            .append(FIELD_BUSYNESS, report.busyness())
            .append(FIELD_CLEANLINESS, report.cleanliness())
            .append(FIELD_ISSUE, report
                .issue()
                .name())
            .append(FIELD_HOUROFDAY, report
                .timestamp()
                .getHour())
            .append(FIELD_TIMESTAMP, Date.from(report
                .timestamp()
                .atZone(ZoneId.systemDefault())
                .toInstant())));
    }

    /**
     * Adds one persistent, varied status report for every hour of every JSON-sourced washroom.
     * @param washrooms parameter value.
     */
    public void ensureJsonHourlyReports(final List<entity.Washroom> washrooms) {
        final LocalDate reportDay = LocalDate
            .now()
            .minusDays(1);
        final Set<String> existingSeedKeys = new HashSet<>();
        for (final Document report : reports.find(Filters.regex(FIELD_SEEDKEY, "^json-hourly-status-"))) {
            existingSeedKeys.add(MongoDocuments.string(report, "", FIELD_SEEDKEY));
        }
        final List<Document> newReports = new ArrayList<>();
        for (int washroomIndex = 0; washroomIndex < washrooms.size(); washroomIndex++) {
            final entity.Washroom washroom = washrooms.get(washroomIndex);
            for (int hour = 0; hour < MAGIC_24; hour++) {
                final String seedKey = "json-hourly-status-" + washroom.id() + "-" + hour;
                if (existingSeedKeys.contains(seedKey)) {
                    continue;
                }
                final int busyness = 1 + Math.floorMod(washroomIndex * 2 + hour * 3, 5);
                final int cleanliness = 1 + Math.floorMod(washroomIndex * 3 + hour * 2, 5);
                final LocalDateTime timestamp = LocalDateTime.of(reportDay, LocalTime.of(hour, 0));
                newReports.add(new Document(FIELD_WASHROOMID, washroom.id())
                    .append(FIELD_USERNAME, "System seed")
                    .append(FIELD_BUSYNESS, busyness)
                    .append(FIELD_CLEANLINESS, cleanliness)
                    .append(FIELD_ISSUE, MaintenanceIssue.NONE.name())
                    .append(FIELD_HOUROFDAY, hour)
                    .append(FIELD_TIMESTAMP, Date.from(timestamp
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                    .append(FIELD_SEEDKEY, seedKey));
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
            if (!MongoDocuments.referenceMatches(document.get(FIELD_WASHROOMID), washroomId)
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
        final Document currentHour = new Document(FIELD__OR, List.of(new Document(FIELD_HOUROFDAY, hour),
            new Document("$expr", new Document("$eq", List.of(new Document("$hour", "$timestamp"), hour)))));
        final List<Document> pipeline = List.of(new Document("$match",
                new Document(FIELD_WASHROOMID, new Document("$in", washroomIds)).append(FIELD__OR,
                    currentHour.get(FIELD__OR))),
            new Document("$sort", new Document(FIELD_TIMESTAMP, -1)), new Document("$group",
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
        reports.updateMany(Filters.exists(FIELD_HOUROFDAY, false),
            List.of(new Document("$set", new Document(FIELD_HOUROFDAY, new Document("$hour", "$timestamp")))));
        reports.createIndex(Indexes.compoundIndex(Indexes.ascending(FIELD_HOUROFDAY),
            Indexes.ascending(FIELD_WASHROOMID),
            Indexes.descending(FIELD_TIMESTAMP)));
    }

    private StatusReport toEntity(final Document document) {
        final String issueName =
            MongoDocuments.string(document, MaintenanceIssue.NONE.name(), FIELD_ISSUE, "maintenanceIssue");
        MaintenanceIssue issue;
        try {
            issue = MaintenanceIssue.valueOf(issueName);
        }
        catch (final IllegalArgumentException ignored) {
            issue = MaintenanceIssue.OTHER;
        }
        return new StatusReport(MongoDocuments.string(document, "unknown", FIELD_WASHROOMID, "washroomID"),
            MongoDocuments.string(document, null, FIELD_USERNAME), clamp(MongoDocuments.integer(document, 1,
                FIELD_BUSYNESS)),
            clamp(MongoDocuments.integer(document, 1, FIELD_CLEANLINESS)), issue,
            MongoDocuments.dateTime(document, LocalDateTime.now(), FIELD_TIMESTAMP, "createdAt"));
    }
}
