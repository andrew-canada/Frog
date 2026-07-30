package app;

import data_access.DBDataAccessObject;
import data_access.building.DBBuildingDataAccessObject;
import data_access.enrollment.DBEnrollmentDataAccessObject;
import data_access.review.DBReviewDataAccessObject;
import data_access.route.GraphhopperRouteDataAccessObject;
import data_access.route.GraphhopperGeocodingDataAccessObject;
import data_access.status.DBStatusReportDataAccessObject;
import data_access.user.DBUserDataAccessObject;
import data_access.washroom.DBWashroomDataAccessObject;
import entity.Washroom;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.change_password.ChangePasswordController;
import interface_adapter.account.change_password.ChangePasswordPresenter;
import interface_adapter.account.change_username.ChangeUsernameController;
import interface_adapter.account.change_username.ChangeUsernamePresenter;
import interface_adapter.account.delete_account.DeleteAccountController;
import interface_adapter.account.delete_account.DeleteAccountPresenter;
import interface_adapter.account.personal_plan.PersonalPlanController;
import interface_adapter.account.personal_plan.PersonalPlanPresenter;
import interface_adapter.busyness.*;
import interface_adapter.directions.*;
import interface_adapter.login.*;
import interface_adapter.recommend.*;
import interface_adapter.status_report.*;
import interface_adapter.view_reviews.*;
import use_case.account.change_password.ChangePasswordInteractor;
import use_case.account.change_username.ChangeUsernameInteractor;
import use_case.account.delete_account.DeleteAccountInteractor;
import use_case.account.personal_plan.PersonalPlanInteractor;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Composition root: the only class that selects concrete database and external-service adapters. */
public final class AppBuilder {
    private static final String MAIN="main",REVIEWS="reviews",LOGIN="login",RECOMMEND="recommend",STATUS="status",BUSYNESS="busyness", ACCOUNT="account";

    public JFrame build() {
        String graphhopperKey = requiredEnvironment(GraphhopperRouteDataAccessObject.API_KEY_ENV);
        DBDataAccessObject connection = DBDataAccessObject.fromEnvironment();
        try { connection.verifyConnection(); }
        catch (RuntimeException failure) { connection.close(); throw new IllegalStateException("Could not connect to the configured MongoDB database.", failure); }

        var database = connection.database();
        var buildings = new DBBuildingDataAccessObject(database);
        var campusLocations = buildings.ensureLocations(UofTCampusLocations.coreLocations());
        var washrooms = new DBWashroomDataAccessObject(database);
        washrooms.ensureCampusWashrooms(campusLocations);
        var reviews = new DBReviewDataAccessObject(database);
        var users = new DBUserDataAccessObject(database);
        var reports = new DBStatusReportDataAccessObject(database);
        var routes = new GraphhopperRouteDataAccessObject(graphhopperKey);
        var geocoding = new GraphhopperGeocodingDataAccessObject(graphhopperKey);
        var enrollment = new DBEnrollmentDataAccessObject(database);

        var reviewsModel=new ReviewsViewModel();
        var listModel=new WashroomListViewModel();
        var loginModel=new LoginViewModel();
        var loggedInModel=new LoggedInViewModel();
        var recommendationModel=new RecommendationViewModel();
        var accountModel=new AccountViewModel();
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
        var changeUsernameController=new ChangeUsernameController(new ChangeUsernameInteractor(users, new ChangeUsernamePresenter(accountModel)));
        var changePasswordController=new ChangePasswordController(new ChangePasswordInteractor(users, new ChangePasswordPresenter(accountModel)));
        var deleteAccountController=new DeleteAccountController(new DeleteAccountInteractor(users, new DeleteAccountPresenter(accountModel)));
        var personalPlanController=new PersonalPlanController(new PersonalPlanInteractor(users, new PersonalPlanPresenter(accountModel)));

        JFrame frame=new JFrame("FlushID — U of T washroom finder");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);frame.setMinimumSize(new Dimension(900,600));frame.setSize(1060,700);
        frame.addWindowListener(new WindowAdapter(){@Override public void windowClosing(WindowEvent event){connection.close();}});
        CardLayout layout=new CardLayout();JPanel cards=new JPanel(layout);
        MainView main = new MainView(listModel, mapModel);
        main.setAddressLookup(geocoding::lookup);

        ReadReviewsView readReviews = new ReadReviewsView(reviewsModel);
        LoginPanel login = new LoginPanel(loginModel, loginController);
        RecommendationView recommendation = new RecommendationView(recommendationModel);

        AccountView account = new AccountView(
            accountModel, 
            changeUsernameController, 
            changePasswordController, 
            deleteAccountController, 
            personalPlanController
        );
        StatusReportView status=new StatusReportView(statusModel);BusynessChartView busyness=new BusynessChartView(busynessModel);
        cards.add(main,MAIN);cards.add(readReviews,REVIEWS);cards.add(login,LOGIN);cards.add(recommendation,RECOMMEND);cards.add(account,ACCOUNT);cards.add(status,STATUS);cards.add(busyness,BUSYNESS);frame.setContentPane(cards);

        Runnable showMain=()->layout.show(cards,MAIN);
        main.setOnReviews(id->{reviewController.execute(id);layout.show(cards,REVIEWS);});
        main.setOnDirections(id->requestDirections(main,directionsController,id));
        main.setOnLogin(()->layout.show(cards,LOGIN));
        main.setOnRecommend(()->layout.show(cards,RECOMMEND));
        main.setOnAccount(() -> layout.show(cards, ACCOUNT));

        main.setOnReport(() -> selected(washrooms, main).ifPresentOrElse(
            w -> {
                status.setWashroomName(w.name());
                layout.show(cards, STATUS);
            },
            () -> noWashroom(frame)
        ));

        main.setOnBusyness(() -> selected(washrooms, main).ifPresentOrElse(
            w -> {
                busynessController.execute(w.id(), w.building().code(), DayOfWeek.from(java.time.LocalDate.now()));
                layout.show(cards, BUSYNESS);
            },
            () -> noWashroom(frame)
        ));

        readReviews.setOnBack(showMain);

        readReviews.setOnWrite(() -> selected(washrooms, main).ifPresent(
            w -> {
                status.setWashroomName(w.name());
                layout.show(cards, STATUS);
            }
        ));
        login.setOnBack(showMain);login.setOnSignup(()->new SignupDialog(frame,signupController).setVisible(true));
        recommendation.setOnBack(showMain);recommendation.setOnFind(()->recommendationController.execute(main.latitude(),main.longitude(),false,null,recommendation.inAHurry(),loggedInModel.getState().username()));
        recommendation.setOnDirections(()->{if(!recommendation.selectedId().isBlank()){requestDirections(main,directionsController,recommendation.selectedId());showMain.run();}});
        recommendation.setOnReviews(()->{if(!recommendation.selectedId().isBlank()){reviewController.execute(recommendation.selectedId());layout.show(cards,REVIEWS);}});
        status.setOnCancel(showMain);status.setOnSubmit(()->{if(!main.selectedId().isBlank())statusController.execute(main.selectedId(),status.busyness(),status.cleanliness(),status.issue(),loggedInModel.getState().loggedIn()?loggedInModel.getState().username():null);});
        busyness.setOnBack(showMain);
        account.setOnBack(showMain);

        double originLat=43.6629,originLng=-79.3957;
        List<Washroom> availableWashrooms=washrooms.getAll();
        main.setWashrooms(availableWashrooms);
        List<WashroomListViewModel.Item> items=availableWashrooms.stream().map(w->new WashroomListViewModel.Item(w.id(),listName(w),w.reviewSummary().averageRating(),
                (int)Math.round(distance(originLat,originLng,w.building().latitude(),w.building().longitude())),w.accessible())).toList();
        String selectedId=items.isEmpty()?null:items.getFirst().id();
        listModel.setState(new WashroomListViewModel.State(items,selectedId,"Sort by: Nearest",false));
        return frame;
    }

    private static void requestDirections(MainView main, DirectionsController controller, String washroomId) {
        main.showRouting();
        CompletableFuture.runAsync(()->controller.execute(main.latitude(),main.longitude(),washroomId));
    }
    private static Optional<Washroom> selected(DBWashroomDataAccessObject washrooms,MainView main){return main.selectedId().isBlank()?Optional.empty():washrooms.getById(main.selectedId());}
    private static void noWashroom(Component parent){JOptionPane.showMessageDialog(parent,"The database does not contain a selectable washroom.","No washrooms",JOptionPane.WARNING_MESSAGE);}
    private static String listName(Washroom washroom){return switch(washroom.building().code()){
        case "BA" -> "Bahen Centre";
        case "MY" -> "Myhal Centre";
        case "TC" -> "Trinity College";
        case "HH" -> "Hart House";
        default -> washroom.name();
    };}
    private static String requiredEnvironment(String name){String value=System.getenv(name);if(value==null||value.isBlank())throw new IllegalStateException("Set the "+name+" environment variable before starting FlushID.");return value;}
    private static double distance(double a,double b,double c,double d){double x=Math.toRadians(d-b)*Math.cos(Math.toRadians((a+c)/2));double y=Math.toRadians(c-a);return Math.sqrt(x*x+y*y)*6_371_000;}
}
