package interface_adapter.status_report;

import entity.MaintenanceIssue;
import use_case.status_report.SubmitStatusReportInputBoundary;
import use_case.status_report.SubmitStatusReportInputData;

public final class StatusReportController {
    private final SubmitStatusReportInputBoundary interactor;

    public StatusReportController(final SubmitStatusReportInputBoundary indexValue) {
        interactor = indexValue;
    }

    /**
     * Performs this operation.
     *
     * @param busy parameter value.
     *
     * @param clean parameter value.
     *
     * @param id parameter value.
     *
     * @param issue parameter value.
     *
     * @param user parameter value.
     */
    public void execute(final String id, final int busy, final int clean, final MaintenanceIssue issue,
                        final String user) {
        interactor.execute(new SubmitStatusReportInputData(id, busy, clean, issue, user));
    }
}
