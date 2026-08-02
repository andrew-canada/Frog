package use_case.status_report;

public record SubmitStatusReportOutputData(boolean success, double currentBusyness, String message) {
}
