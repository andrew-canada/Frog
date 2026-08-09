package interface_adapter.status_report;

import use_case.status_report.SubmitStatusReportOutputBoundary;
import use_case.status_report.SubmitStatusReportOutputData;

public final class StatusReportPresenter implements SubmitStatusReportOutputBoundary {
    private final StatusReportViewModel model;

    public StatusReportPresenter(final StatusReportViewModel model) {
        this.model = model;
    }

    @Override
    public void present(final SubmitStatusReportOutputData d) {
        model.setState(new StatusReportViewModel.State(d.success(), d.currentBusyness(), d.message()));
    }
}
