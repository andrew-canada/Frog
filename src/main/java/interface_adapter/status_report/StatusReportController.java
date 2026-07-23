package interface_adapter.status_report;
import entity.MaintenanceIssue;
import use_case.status_report.SubmitStatusReportInputBoundary;
import use_case.status_report.SubmitStatusReportInputData;
public final class StatusReportController{
    private final SubmitStatusReportInputBoundary interactor; public StatusReportController(SubmitStatusReportInputBoundary i){interactor=i;}
    public void execute(String id,int busy,int clean,MaintenanceIssue issue,String user){interactor.execute(new SubmitStatusReportInputData(id,busy,clean,issue,user));}
}
