package data_access.status;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
