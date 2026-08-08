package use_case.status_report;

import data_access.status.StatusReportDataAccessInterface;
import entity.StatusReport;

import java.time.Clock;
import java.time.LocalDateTime;

public final class SubmitStatusReportInteractor implements SubmitStatusReportInputBoundary {
    private final StatusReportDataAccessInterface reports;
    private final SubmitStatusReportOutputBoundary presenter;
    private final Clock clock;

    public SubmitStatusReportInteractor(StatusReportDataAccessInterface reports, SubmitStatusReportOutputBoundary presenter) {
        this(reports, presenter, Clock.systemDefaultZone());
    }

    public SubmitStatusReportInteractor(StatusReportDataAccessInterface reports, SubmitStatusReportOutputBoundary presenter, Clock clock) {
        this.reports = reports;
        this.presenter = presenter;
        this.clock = clock;
    }

    @Override
    public void execute(SubmitStatusReportInputData in) {
        if (in.busyness() < 1 || in.busyness() > 5 || in.cleanliness() < 1 || in.cleanliness() > 5) {
            presenter.present(new SubmitStatusReportOutputData(false, 0, "Choose values from 1 to 5"));
            return;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        reports.save(new StatusReport(in.washroomId(), in.username(), in.busyness(), in.cleanliness(), in.issue(), now));
        double current = reports.getRecentForWashroom(in.washroomId(), now.minusHours(4)).stream()
                .mapToInt(StatusReport::busyness).average().orElse(in.busyness());
        presenter.present(new SubmitStatusReportOutputData(true, current, "Thanks — live status updated"));
    }
}
