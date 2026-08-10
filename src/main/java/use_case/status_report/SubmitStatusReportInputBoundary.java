package use_case.status_report;

public interface SubmitStatusReportInputBoundary {
    /**
     * Performs this operation.
     *
     * @param inputData parameter value.
     */
    void execute(SubmitStatusReportInputData inputData);
}
