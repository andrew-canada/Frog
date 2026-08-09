package data_access.enrollment;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import data_access.MongoDocuments;
import entity.EnrollmentMeeting;
import org.bson.Document;
import use_case.port.EnrollmentScheduleGateway;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads timetable-derived meeting rows from MongoDB; an absent collection means no prediction signal.
 */
public final class DBEnrollmentDataAccessObject implements EnrollmentScheduleGateway {
    private final MongoCollection<Document> meetings;

    public DBEnrollmentDataAccessObject(MongoDatabase database) {
        meetings = database.getCollection("EnrollmentMeetings");
    }

    @Override
    public List<EnrollmentMeeting> getBuildingSchedule(String buildingCode, DayOfWeek dayOfWeek) {
        List<EnrollmentMeeting> result = new ArrayList<>();
        for (Document document : meetings.find(new Document("buildingCode", buildingCode))) {
            String storedDay = MongoDocuments.string(document, "", "dayOfWeek", "day");
            if (!storedDay.isBlank() && !storedDay.equalsIgnoreCase(dayOfWeek.name())) continue;
            int start = MongoDocuments.integer(document, 0, "startHour");
            int end = MongoDocuments.integer(document, 0, "endHour");
            int enrollment = Math.max(0, MongoDocuments.integer(document, 0, "enrollment", "enrolment"));
            if (start >= 0 && end > start && end <= 24) result.add(new EnrollmentMeeting(start, end, enrollment));
        }
        return List.copyOf(result);
    }
}
