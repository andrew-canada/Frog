package app;

import data_access.enrollment.TimetableEnrollmentDataAccessObject;
import data_access.review.InMemoryReviewDataAccessObject;
import data_access.route.GraphhopperRouteDataAccessObject;
import data_access.status.InMemoryStatusReportDataAccessObject;
import data_access.user.InMemoryUserDataAccessObject;
import data_access.washroom.InMemoryWashroomDataAccessObject;
import entity.Washroom;
import interface_adapter.busyness.*;
import interface_adapter.directions.*;
import interface_adapter.login.*;
import interface_adapter.recommend.*;
import interface_adapter.status_report.*;
import interface_adapter.view_reviews.*;
import use_case.busyness.BusynessStatsInteractor;
import use_case.directions.GetDirectionsInteractor;
import use_case.login.LoginInteractor;
import use_case.recommend.RecommendWashroomInteractor;
import use_case.signup.SignupInteractor;
import use_case.status_report.SubmitStatusReportInteractor;
import use_case.view_reviews.ViewReviewsInteractor;
import view.*;
import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.util.List;

/** Composition root: this is the only place concrete adapters are selected and wired. */
public final class AppBuilder {
    private static final String MAIN="main",REVIEWS="reviews",LOGIN="login",RECOMMEND="recommend",STATUS="status",BUSYNESS="busyness";

    public JFrame build() {
        var washrooms=new InMemoryWashroomDataAccessObject();
        var reviews=new InMemoryReviewDataAccessObject();
        var users=new InMemoryUserDataAccessObject();
        var reports=new InMemoryStatusReportDataAccessObject();
        var routes=new GraphhopperRouteDataAccessObject();
        var enrollment=new TimetableEnrollmentDataAccessObject();

        var reviewsModel=new ReviewsViewModel();
        var listModel=new WashroomListViewModel();
        var loginModel=new LoginViewModel();
        var loggedInModel=new LoggedInViewModel();
        var recommendationModel=new RecommendationViewModel();
        var statusModel=new StatusReportViewModel();
        var busynessModel=new BusynessViewModel();
        var mapModel=new MapViewModel();

        var reviewController=new ViewReviewsController(new ViewReviewsInteractor(reviews,washrooms,new ViewReviewsPresenter(reviewsModel)));
        var loginController=new LoginController(new LoginInteractor(users,new LoginPresenter(loginModel,loggedInModel)));
        var signupController=new SignupController(new SignupInteractor(users,new SignupPresenter(loginModel,loggedInModel)));
        var recommendationController=new RecommendationController(new RecommendWashroomInteractor(washrooms,reports,new RecommendationPresenter(recommendationModel)));
        var statusController=new StatusReportController(new SubmitStatusReportInteractor(reports,new StatusReportPresenter(statusModel)));
        var busynessController=new BusynessController(new BusynessStatsInteractor(reports,enrollment,new BusynessPresenter(busynessModel)));
        var directionsController=new DirectionsController(new GetDirectionsInteractor(washrooms,routes,new DirectionsPresenter(mapModel)));

        JFrame frame=new JFrame("FlushID — U of T washroom finder");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);frame.setMinimumSize(new Dimension(900,600));frame.setSize(1060,700);
        CardLayout layout=new CardLayout();JPanel cards=new JPanel(layout);
        MainView main=new MainView(listModel,mapModel);ReadReviewsView readReviews=new ReadReviewsView(reviewsModel);
        LoginView login=new LoginView(loginModel,loginController);RecommendationView recommendation=new RecommendationView(recommendationModel);
        StatusReportView status=new StatusReportView(statusModel);BusynessChartView busyness=new BusynessChartView(busynessModel);
        cards.add(main,MAIN);cards.add(readReviews,REVIEWS);cards.add(login,LOGIN);cards.add(recommendation,RECOMMEND);cards.add(status,STATUS);cards.add(busyness,BUSYNESS);frame.setContentPane(cards);

        Runnable showMain=()->layout.show(cards,MAIN);
        main.setOnReviews(id->{reviewController.execute(id);layout.show(cards,REVIEWS);});
        main.setOnDirections(id->directionsController.execute(main.latitude(),main.longitude(),id));
        main.setOnLogin(()->layout.show(cards,LOGIN));
        main.setOnRecommend(()->layout.show(cards,RECOMMEND));
        main.setOnReport(()->layout.show(cards,STATUS));
        main.setOnBusyness(()->{Washroom w=washrooms.getById(main.selectedId()).orElseThrow();busynessController.execute(w.id(),w.building().code(),DayOfWeek.THURSDAY);layout.show(cards,BUSYNESS);});
        readReviews.setOnBack(showMain);readReviews.setOnWrite(()->layout.show(cards,STATUS));
        login.setOnBack(showMain);login.setOnSignup(()->new SignupDialog(frame,signupController).setVisible(true));
        recommendation.setOnBack(showMain);recommendation.setOnFind(()->recommendationController.execute(main.latitude(),main.longitude(),false,null,recommendation.inAHurry(),loggedInModel.getState().username()));
        recommendation.setOnDirections(()->{if(!recommendation.selectedId().isBlank()){directionsController.execute(main.latitude(),main.longitude(),recommendation.selectedId());showMain.run();}});
        recommendation.setOnReviews(()->{if(!recommendation.selectedId().isBlank()){reviewController.execute(recommendation.selectedId());layout.show(cards,REVIEWS);}});
        status.setOnCancel(showMain);status.setOnSubmit(()->statusController.execute(main.selectedId(),status.busyness(),status.cleanliness(),status.issue(),loggedInModel.getState().loggedIn()?loggedInModel.getState().username():null));
        busyness.setOnBack(showMain);

        double originLat=43.6629,originLng=-79.3957;
        List<WashroomListViewModel.Item> items=washrooms.getAll().stream().map(w->new WashroomListViewModel.Item(w.id(),w.name(),w.reviewSummary().averageRating(),
                (int)Math.round(distance(originLat,originLng,w.building().latitude(),w.building().longitude())),w.accessible())).toList();
        listModel.setState(new WashroomListViewModel.State(items,"bahen-2","Sort by: Nearest",false));
        return frame;
    }

    private static double distance(double a,double b,double c,double d){double x=Math.toRadians(d-b)*Math.cos(Math.toRadians((a+c)/2));double y=Math.toRadians(c-a);return Math.sqrt(x*x+y*y)*6_371_000;}
}
