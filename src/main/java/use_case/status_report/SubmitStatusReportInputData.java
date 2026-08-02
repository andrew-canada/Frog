package use_case.status_report;

import entity.MaintenanceIssue;

public record SubmitStatusReportInputData(String washroomId, int busyness, int cleanliness,
                                          MaintenanceIssue issue, String username) {
}
