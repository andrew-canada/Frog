package use_case.status_report;

public interface SubmitStatusReportOutputBoundary {
    /**
     * Performs this operation.
     *
     * @param outputData parameter value.
     */
    void present(SubmitStatusReportOutputData outputData);
}
