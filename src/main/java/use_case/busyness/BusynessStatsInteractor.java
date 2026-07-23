package use_case.busyness;

import entity.EnrollmentMeeting;
import entity.StatusReport;
import use_case.gateway.EnrollmentDataAccessInterface;
import use_case.gateway.StatusReportDataAccessInterface;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class BusynessStatsInteractor implements BusynessStatsInputBoundary {
    private final StatusReportDataAccessInterface reports;
    private final EnrollmentDataAccessInterface enrollment;
    private final BusynessStatsOutputBoundary presenter;
    public BusynessStatsInteractor(StatusReportDataAccessInterface reports, EnrollmentDataAccessInterface enrollment,
                                   BusynessStatsOutputBoundary presenter) {
        this.reports=reports; this.enrollment=enrollment; this.presenter=presenter;
    }
    @Override public void execute(BusynessStatsInputData in) {
        LocalDateTime now=LocalDateTime.now();
        List<StatusReport> observations=reports.getForWashroom(in.washroomId(), now.minusDays(30), now.plusSeconds(1));
        List<EnrollmentMeeting> meetings=enrollment.getBuildingSchedule(in.buildingCode(), in.dayOfWeek());
        List<BusynessStatsOutputData.HourBucket> buckets=new ArrayList<>();
        for (int hour=8; hour<=20; hour++) {
            final int h=hour;
            double crowd=observations.stream().filter(r->r.timestamp().getHour()==h).mapToInt(StatusReport::busyness).average().orElse(Double.NaN);
            int students=meetings.stream().filter(m->h>=m.startHour() && h<m.endHour()).mapToInt(EnrollmentMeeting::enrollment).sum();
            double predicted=Math.min(5, 1 + students/100.0);
            boolean hasCrowd=!Double.isNaN(crowd);
            double blended=hasCrowd ? crowd*.65 + predicted*.35 : predicted;
            buckets.add(new BusynessStatsOutputData.HourBucket(hour, Math.max(1, Math.min(5, blended)), hasCrowd?"reports":"timetable"));
        }
        presenter.present(new BusynessStatsOutputData(buckets, "Crowdsourced reports blended with timetable enrolment"));
    }
}
