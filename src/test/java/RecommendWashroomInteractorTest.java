import entity.*;
import use_case.gateway.*;
import use_case.recommend.*;
import java.time.LocalDateTime;
import java.util.*;

final class RecommendWashroomInteractorTest {
    static void run(){Washroom w=TestSupport.washroom();WashroomDataAccessInterface washrooms=new WashroomDataAccessInterface(){public Optional<Washroom> getById(String id){return Optional.of(w);}public List<Washroom> getNearby(double a,double b,double r){return List.of(w);}public List<Washroom> getAll(){return List.of(w);}};
        StatusReportDataAccessInterface reports=new StatusReportDataAccessInterface(){public void save(StatusReport r){}public List<StatusReport> getRecentForWashroom(String id,LocalDateTime since){return List.of(new StatusReport(id,null,2,5,MaintenanceIssue.NONE,LocalDateTime.now()));}public List<StatusReport> getForWashroom(String id,LocalDateTime f,LocalDateTime t){return List.of();}};
        final RecommendWashroomOutputData[] out=new RecommendWashroomOutputData[1];RecommendWashroomOutputBoundary presenter=new RecommendWashroomOutputBoundary(){public void present(RecommendWashroomOutputData d){out[0]=d;}public void presentError(String m){throw new AssertionError(m);}};
        new RecommendWashroomInteractor(washrooms,reports,presenter).execute(new RecommendWashroomInputData(43.66,-79.39,true,null,false,null));
        TestSupport.check("w1".equals(out[0].washroomId())&&out[0].scoreBreakdown().size()==4,"recommendation and score explanation");}
}
