package interface_adapter.status_report;

import interface_adapter.common.ViewModel;

public final class StatusReportViewModel extends ViewModel<StatusReportViewModel.State>{
    public StatusReportViewModel(){super(new State(false,0,""));}
    public record State(boolean success,double currentBusyness,String message){}
}
