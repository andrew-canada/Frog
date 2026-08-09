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
                .filter(r -> {
                    return r
                        .timestamp()
                        .getHour() == h;
                })
                .max(Comparator.comparing(StatusReport::timestamp))
                .orElse(null);
            final double crowd;
            if (latestReport == null) {
                crowd = Double.NaN;
            }
            else {
                crowd = latestReport.busyness();
            }
            final double cleanliness;
            if (latestReport == null) {
                cleanliness = 0;
            }
            else {
                cleanliness = latestReport.cleanliness();
            }
            final int students = meetings
                .stream()
                .filter(m -> {
                    return h >= m.startHour() && h < m.endHour();
                })
                .mapToInt(EnrollmentMeeting::enrollment)
                .sum();
            final boolean hasTimetable = !meetings.isEmpty();
            final double predicted;
            if (hasTimetable) {
                predicted = Math.min(5, 1 + students / 100.0);
            }
            else {
                predicted = Double.NaN;
            }
            final boolean hasCrowd = !Double.isNaN(crowd);
            final double level;
            if (hasCrowd) {
                level = crowd;
            }
            else {
                if (hasTimetable) {
                    level = predicted;
                }
                else {
                    level = 0;
                }
            }
            if (hasCrowd) {
                buckets.add(new BusynessStatsOutputData.HourBucket(hour, Math.max(0, Math.min(5, level)), cleanliness,
                    "latest report"));
            }
            else {
                buckets.add(new BusynessStatsOutputData.HourBucket(hour, Math.max(0, Math.min(5, level)), cleanliness,
                    hasTimetable ? "enrollment" : "no data"));
            }
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
}
