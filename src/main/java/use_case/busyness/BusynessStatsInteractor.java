package use_case.busyness;

import use_case.port.EnrollmentScheduleGateway;
import use_case.port.StatusReportRepository;
import entity.EnrollmentMeeting;
import entity.StatusReport;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BusynessStatsInteractor implements BusynessStatsInputBoundary {
    private final StatusReportRepository reports;
    private final EnrollmentScheduleGateway enrollment;
    private final BusynessStatsOutputBoundary presenter;

    public BusynessStatsInteractor(StatusReportRepository reports, EnrollmentScheduleGateway enrollment,
                                   BusynessStatsOutputBoundary presenter) {
        this.reports = reports;
        this.enrollment = enrollment;
        this.presenter = presenter;
    }

    @Override
    public void execute(BusynessStatsInputData in) {
        LocalDateTime now = LocalDateTime.now();
        List<StatusReport> observations = reports.getForWashroom(in.washroomId(), now.minusDays(30), now.plusSeconds(1));
        List<EnrollmentMeeting> meetings = enrollment.getBuildingSchedule(in.buildingCode(), in.dayOfWeek());
        List<BusynessStatsOutputData.HourBucket> buckets = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            final int h = hour;
            StatusReport latestReport = observations.stream().filter(r -> r.timestamp().getHour() == h)
                    .max(Comparator.comparing(StatusReport::timestamp)).orElse(null);
            double crowd = latestReport == null ? Double.NaN : latestReport.busyness();
            double cleanliness = latestReport == null ? 0 : latestReport.cleanliness();
            int students = meetings.stream().filter(m -> h >= m.startHour() && h < m.endHour()).mapToInt(EnrollmentMeeting::enrollment).sum();
            boolean hasTimetable = !meetings.isEmpty();
            double predicted = hasTimetable ? Math.min(5, 1 + students / 100.0) : Double.NaN;
            boolean hasCrowd = !Double.isNaN(crowd);
            double level = hasCrowd ? crowd : hasTimetable ? predicted : 0;
            buckets.add(new BusynessStatsOutputData.HourBucket(hour, Math.max(0, Math.min(5, level)), cleanliness,
                    hasCrowd ? "latest report" : hasTimetable ? "enrollment" : "no data"));
        }
        String note = observations.isEmpty() && meetings.isEmpty() ? "No status or enrollment data is stored yet"
                : "MongoDB status reports blended with stored timetable enrollment";
        presenter.present(new BusynessStatsOutputData(buckets, note));
    }
}
