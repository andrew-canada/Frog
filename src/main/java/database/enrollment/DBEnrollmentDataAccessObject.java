package database.enrollment;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import database.MongoDocuments;
import entity.EnrollmentMeeting;
import use_case.port.EnrollmentScheduleGateway;

/**
 * Reads timetable-derived meeting rows from MongoDB; an absent collection means no prediction signal.
 */
public final class DBEnrollmentDataAccessObject implements EnrollmentScheduleGateway {
    private static final int HOURS_PER_DAY = 24;
    private final MongoCollection<Document> meetings;

    public DBEnrollmentDataAccessObject(final MongoDatabase database) {
        meetings = database.getCollection("EnrollmentMeetings");
    }

    @Override
    public List<EnrollmentMeeting> getBuildingSchedule(final String buildingCode, final DayOfWeek dayOfWeek) {
        final List<EnrollmentMeeting> result = new ArrayList<>();
        for (final Document document : meetings.find(new Document("buildingCode", buildingCode))) {
            final String storedDay = MongoDocuments.string(document, "", "dayOfWeek", "day");
            if (!storedDay.isBlank() && !storedDay.equalsIgnoreCase(dayOfWeek.name())) {
                continue;
            }
            final int start = MongoDocuments.integer(document, 0, "startHour");
            final int end = MongoDocuments.integer(document, 0, "endHour");
            final int enrollment = Math.max(0, MongoDocuments.integer(document, 0, "enrollment", "enrolment"));
            if (start >= 0 && end > start && end <= HOURS_PER_DAY) {
                result.add(new EnrollmentMeeting(start, end, enrollment));
            }
        }
        return List.copyOf(result);
    }
}
