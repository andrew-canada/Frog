package use_case.busyness;

import entity.EnrollmentMeeting;
import entity.StatusReport;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import use_case.port.EnrollmentScheduleGateway;
import use_case.port.StatusReportRepository;

public final class BusynessStatsInteractor implements BusynessStatsInputBoundary {
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
        for (int hour = 0; hour < 24; hour++) {
            final int h = hour;
            final StatusReport latestReport = observations
                .stream()
                .filter(r -> r
                    .timestamp()
                    .getHour() == h)
                .max(Comparator.comparing(StatusReport::timestamp))
                .orElse(null);
            final double crowd = latestReport == null ? Double.NaN : latestReport.busyness();
            final double cleanliness = latestReport == null ? 0 : latestReport.cleanliness();
            final int students = meetings
                .stream()
                .filter(m -> h >= m.startHour() && h < m.endHour())
                .mapToInt(EnrollmentMeeting::enrollment)
                .sum();
            final boolean hasTimetable = !meetings.isEmpty();
            final double predicted = hasTimetable ? Math.min(5, 1 + students / 100.0) : Double.NaN;
            final boolean hasCrowd = !Double.isNaN(crowd);
            final double level = hasCrowd ? crowd : hasTimetable ? predicted : 0;
            buckets.add(new BusynessStatsOutputData.HourBucket(hour, Math.max(0, Math.min(5, level)), cleanliness,
                hasCrowd ? "latest report" : hasTimetable ? "enrollment" : "no data"));
        }
        final String note = observations.isEmpty() && meetings.isEmpty() ? "No status or enrollment data is stored yet"
            : "MongoDB status reports blended with stored timetable enrollment";
        presenter.present(new BusynessStatsOutputData(buckets, note));
    }
}
