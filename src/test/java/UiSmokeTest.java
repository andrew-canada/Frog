import interface_adapter.busyness.BusynessViewModel;
import interface_adapter.directions.MapViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginViewModel;
import interface_adapter.recommend.RecommendationViewModel;
import interface_adapter.status_report.StatusReportViewModel;
import interface_adapter.view_reviews.ReviewsViewModel;
import interface_adapter.view_reviews.WashroomListViewModel;
import view.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

final class UiSmokeTest {
    static void run(){
        System.setProperty("java.awt.headless","true");
        try{SwingUtilities.invokeAndWait(()->{
            var list=new WashroomListViewModel();var reviews=new ReviewsViewModel();var map=new MapViewModel();
            List<JPanel> views=List.of(new MainView(list,map),new ReadReviewsView(reviews),
                    new LoginView(new LoginViewModel(),new LoginController(input->{})),
                    new RecommendationView(new RecommendationViewModel()),new StatusReportView(new StatusReportViewModel()),
                    new BusynessChartView(new BusynessViewModel()),new FilterPanel());
            list.setState(new WashroomListViewModel.State(List.of(new WashroomListViewModel.Item("w1","Test washroom",4.5,200,true)),"w1","Nearest",false));
            for(JPanel view:views){view.setSize(900,600);layoutTree(view);BufferedImage image=new BufferedImage(900,600,BufferedImage.TYPE_INT_ARGB);view.paint(image.getGraphics());
                TestSupport.check(view.getComponentCount()>0,view.getClass().getSimpleName()+" should contain UI controls");}
        });}catch(Exception e){throw new AssertionError("UI smoke rendering failed",e);}
    }
    private static void layoutTree(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container nested)layoutTree(nested);}
}
