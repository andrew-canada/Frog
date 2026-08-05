package use_case.busyness;

import entity.EnrollmentMeeting;
import entity.StatusReport;
import data_access.enrollment.EnrollmentDataAccessInterface;
import data_access.status.StatusReportDataAccessInterface;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class BusynessStatsInteractor implements BusynessStatsInputBoundary {
    private final StatusReportDataAccessInterface reports;
    private final EnrollmentDataAccessInterface enrollment;
    private final BusynessStatsOutputBoundary presenter;

    public BusynessStatsInteractor(StatusReportDataAccessInterface reports, EnrollmentDataAccessInterface enrollment,
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
        for (int hour = 8; hour <= 20; hour++) {
            final int h = hour;
            double crowd = observations.stream().filter(r -> r.timestamp().getHour() == h).mapToInt(StatusReport::busyness).average().orElse(Double.NaN);
            int students = meetings.stream().filter(m -> h >= m.startHour() && h < m.endHour()).mapToInt(EnrollmentMeeting::enrollment).sum();
            boolean hasTimetable = !meetings.isEmpty();
            double predicted = hasTimetable ? Math.min(5, 1 + students / 100.0) : Double.NaN;
            boolean hasCrowd = !Double.isNaN(crowd);
            double blended = hasCrowd && hasTimetable ? crowd * .65 + predicted * .35 : hasCrowd ? crowd : hasTimetable ? predicted : 0;
            buckets.add(new BusynessStatsOutputData.HourBucket(hour, Math.max(0, Math.min(5, blended)),
                    hasCrowd && hasTimetable ? "reports + enrollment" : hasCrowd ? "reports" : hasTimetable ? "enrollment" : "no data"));
        }
        String note = observations.isEmpty() && meetings.isEmpty() ? "No status or enrollment data is stored yet"
                : "MongoDB status reports blended with stored timetable enrollment";
        presenter.present(new BusynessStatsOutputData(buckets, note));
    }
}
