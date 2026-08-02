package data_access.status;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import data_access.MongoDocuments;
import entity.MaintenanceIssue;
import entity.StatusReport;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
