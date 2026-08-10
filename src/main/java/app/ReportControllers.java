package app;

import interface_adapter.busyness.BusynessController;
import interface_adapter.busyness.BusynessPresenter;
import interface_adapter.status_report.StatusReportController;
import interface_adapter.status_report.StatusReportPresenter;
import use_case.busyness.BusynessStatsInteractor;
import use_case.status_report.SubmitStatusReportInteractor;

/** Status-report and busyness controller construction. */
final class ReportControllers {
    private final StatusReportController status;
    private final BusynessController busyness;

    private ReportControllers(final StatusReportController status, final BusynessController busyness) {
        this.status = status;
        this.busyness = busyness;
    }

    static ReportControllers create(final ApplicationInfrastructure infrastructure, final FeatureModels features) {
        return new ReportControllers(
            new StatusReportController(new SubmitStatusReportInteractor(infrastructure.reports(),
                new StatusReportPresenter(features.status()))),
            new BusynessController(new BusynessStatsInteractor(infrastructure.reports(), infrastructure.enrollment(),
                new BusynessPresenter(features.busyness()))));
    }

    StatusReportController status() {
        return status;
    }

    BusynessController busyness() {
        return busyness;
    }
}
