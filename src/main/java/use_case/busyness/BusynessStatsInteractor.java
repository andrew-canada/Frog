package use_case.busyness;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import entity.EnrollmentMeeting;
import entity.StatusReport;
import use_case.port.EnrollmentScheduleGateway;
import use_case.port.StatusReportRepository;

public final class BusynessStatsInteractor implements BusynessStatsInputBoundary {
    private static final int MAX_BUSYNESS_LEVEL = 5;
    private static final double STUDENTS_PER_BUSYNESS_LEVEL = 100.0;
    private static final int HOURS_PER_DAY = 24;
    private final StatusReportRepository reports;
    private final EnrollmentScheduleGateway enrollment;
    private final BusynessStatsOutputBoundary presenter;

    public BusynessStatsInteractor(final StatusReportRepository reports, final EnrollmentScheduleGateway enrollment,
                                   final BusynessStatsOutputBoundary presenter) {
        this.reports = reports;
        this.enrollment = enrollment;
        this.presenter = presenter;
    }

    @Override
    public void execute(final BusynessStatsInputData in) {
        final LocalDateTime now = LocalDateTime.now();
        final List<StatusReport> observations =
            reports.getForWashroom(in.washroomId(), now.minusDays(30), now.plusSeconds(1));
        final List<EnrollmentMeeting> meetings = enrollment.getBuildingSchedule(in.buildingCode(), in.dayOfWeek());
        final List<BusynessStatsOutputData.HourBucket> buckets = new ArrayList<>();
        for (int hour = 0; hour < HOURS_PER_DAY; hour++) {
            buckets.add(hourBucket(hour, observations, meetings));
        }
        final String note;
        if (observations.isEmpty() && meetings.isEmpty()) {
            note = "No status or enrollment data is stored yet";
        }
        else {
            note = "MongoDB status reports blended with stored timetable enrollment";
        }
        presenter.present(new BusynessStatsOutputData(buckets, note));
    }

    private static BusynessStatsOutputData.HourBucket hourBucket(final int hour,
                                                                  final List<StatusReport> observations,
                                                                  final List<EnrollmentMeeting> meetings) {
        final StatusReport latestReport = observations
            .stream()
            .filter(reviewValue -> reviewValue.timestamp().getHour() == hour)
            .max(Comparator.comparing(StatusReport::timestamp))
            .orElse(null);
        final double crowd;
        final double cleanliness;
        if (latestReport == null) {
            crowd = Double.NaN;
            cleanliness = 0;
        }
        else {
            crowd = latestReport.busyness();
            cleanliness = latestReport.cleanliness();
        }
        final int students = meetings
            .stream()
            .filter(parameterValue -> hour >= parameterValue.startHour() && hour < parameterValue.endHour())
            .mapToInt(EnrollmentMeeting::enrollment)
            .sum();
        final boolean hasTimetable = !meetings.isEmpty();
        final double predicted;
        if (hasTimetable) {
            predicted = Math.min(MAX_BUSYNESS_LEVEL, 1 + students / STUDENTS_PER_BUSYNESS_LEVEL);
        }
        else {
            predicted = Double.NaN;
        }
        final boolean hasCrowd = !Double.isNaN(crowd);
        final double level;
        if (hasCrowd) {
            level = crowd;
        }
        else if (hasTimetable) {
            level = predicted;
        }
        else {
            level = 0;
        }
        return new BusynessStatsOutputData.HourBucket(hour, Math.max(0, Math.min(MAX_BUSYNESS_LEVEL, level)),
            cleanliness, bucketSource(hasCrowd, hasTimetable));
    }

    private static String bucketSource(final boolean hasCrowd, final boolean hasTimetable) {
        final String result;
        if (hasCrowd) {
            result = "latest report";
        }
        else if (hasTimetable) {
            result = "enrollment";
        }
        else {
            result = "no data";
        }
        return result;
    }
}
