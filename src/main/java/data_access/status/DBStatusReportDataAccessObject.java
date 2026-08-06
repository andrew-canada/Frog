package data_access.status;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import data_access.MongoDocuments;
import entity.MaintenanceIssue;
import entity.StatusReport;
import org.bson.Document;

import com.mongodb.client.model.Filters;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.Set;

public final class DBStatusReportDataAccessObject implements StatusReportDataAccessInterface {
    private final MongoCollection<Document> reports;

    public DBStatusReportDataAccessObject(MongoDatabase database) {
        reports = database.getCollection("StatusReports");
    }

    @Override
    public void save(StatusReport report) {
        reports.insertOne(new Document("washroomId", report.washroomId())
                .append("username", report.username()).append("busyness", report.busyness())
                .append("cleanliness", report.cleanliness()).append("issue", report.issue().name())
                .append("hourOfDay", report.timestamp().getHour())
                .append("timestamp", Date.from(report.timestamp().atZone(ZoneId.systemDefault()).toInstant())));
    }

    /** Adds one persistent, varied status report for every hour of every JSON-sourced washroom. */
    public void ensureJsonHourlyReports(List<entity.Washroom> washrooms) {
        LocalDate reportDay = LocalDate.now().minusDays(1);
        Set<String> existingSeedKeys = new HashSet<>();
        for (Document report : reports.find(Filters.regex("seedKey", "^json-hourly-status-"))) {
            existingSeedKeys.add(MongoDocuments.string(report, "", "seedKey"));
        }
        List<Document> newReports = new ArrayList<>();
        for (int washroomIndex = 0; washroomIndex < washrooms.size(); washroomIndex++) {
            entity.Washroom washroom = washrooms.get(washroomIndex);
            for (int hour = 0; hour < 24; hour++) {
                String seedKey = "json-hourly-status-" + washroom.id() + "-" + hour;
                if (existingSeedKeys.contains(seedKey)) continue;
                int busyness = 1 + Math.floorMod(washroomIndex * 2 + hour * 3, 5);
                int cleanliness = 1 + Math.floorMod(washroomIndex * 3 + hour * 2, 5);
                LocalDateTime timestamp = LocalDateTime.of(reportDay, LocalTime.of(hour, 0));
                newReports.add(new Document("washroomId", washroom.id())
                        .append("username", "System seed")
                        .append("busyness", busyness)
                        .append("cleanliness", cleanliness)
                        .append("issue", MaintenanceIssue.NONE.name())
                        .append("hourOfDay", hour)
                        .append("timestamp", Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant()))
                        .append("seedKey", seedKey));
            }
        }
        if (!newReports.isEmpty()) reports.insertMany(newReports);
    }

    @Override
    public List<StatusReport> getRecentForWashroom(String washroomId, LocalDateTime since) {
        return getForWashroom(washroomId, since, LocalDateTime.now().plusSeconds(1));
    }

    @Override
    public List<StatusReport> getForWashroom(String washroomId, LocalDateTime from, LocalDateTime to) {
        List<StatusReport> result = new ArrayList<>();
        for (Document document : reports.find()) {
            if (!MongoDocuments.referenceMatches(document.get("washroomId"), washroomId)
                    && !MongoDocuments.referenceMatches(document.get("washroomID"), washroomId)) continue;
            StatusReport report = toEntity(document);
            if (!report.timestamp().isBefore(from) && !report.timestamp().isAfter(to)) result.add(report);
        }
        return List.copyOf(result);
    }

    /** Returns each requested washroom's newest report in one current-hour aggregation. */
    @Override
    public Map<String, StatusReport> getCurrentHourForWashrooms(List<String> washroomIds, int hour) {
        if (washroomIds.isEmpty()) return Map.of();
        Document currentHour = new Document("$or", List.of(
                new Document("hourOfDay", hour),
                new Document("$expr", new Document("$eq", List.of(new Document("$hour", "$timestamp"), hour)))));
        List<Document> pipeline = List.of(
                new Document("$match", new Document("washroomId", new Document("$in", washroomIds))
                        .append("$or", currentHour.get("$or"))),
                new Document("$sort", new Document("timestamp", -1)),
                new Document("$group", new Document("_id", "$washroomId")
                        .append("latest", new Document("$first", "$$ROOT"))),
                new Document("$replaceRoot", new Document("newRoot", "$latest")));
        Map<String, StatusReport> result = new HashMap<>();
        for (Document document : reports.aggregate(pipeline)) {
            StatusReport report = toEntity(document);
            result.put(report.washroomId(), report);
        }
        return Map.copyOf(result);
    }

    /** Backfills the hour bucket once, then indexes the current-hour heatmap query. */
    public void ensurePerformanceIndexes() {
        reports.updateMany(Filters.exists("hourOfDay", false), List.of(
                new Document("$set", new Document("hourOfDay", new Document("$hour", "$timestamp")))));
        reports.createIndex(Indexes.compoundIndex(Indexes.ascending("hourOfDay"),
                Indexes.ascending("washroomId"), Indexes.descending("timestamp")));
    }

    private StatusReport toEntity(Document document) {
        String issueName = MongoDocuments.string(document, MaintenanceIssue.NONE.name(), "issue", "maintenanceIssue");
        MaintenanceIssue issue;
        try {
            issue = MaintenanceIssue.valueOf(issueName);
        } catch (IllegalArgumentException ignored) {
            issue = MaintenanceIssue.OTHER;
        }
        return new StatusReport(MongoDocuments.string(document, "unknown", "washroomId", "washroomID"),
                MongoDocuments.string(document, null, "username"),
                clamp(MongoDocuments.integer(document, 1, "busyness")),
                clamp(MongoDocuments.integer(document, 1, "cleanliness")), issue,
                MongoDocuments.dateTime(document, LocalDateTime.now(), "timestamp", "createdAt"));
    }

    private static int clamp(int value) {
        return Math.max(1, Math.min(5, value));
    }
}
