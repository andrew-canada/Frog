package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoDatabase;

import database.DBDataAccessObject;
import database.enrollment.DBEnrollmentDataAccessObject;
import database.review.DBReviewDataAccessObject;
import database.route.GraphhopperGeocodingDataAccessObject;
import database.route.GraphhopperRouteDataAccessObject;
import database.status.DBStatusReportDataAccessObject;
import database.user.DBUserDataAccessObject;
import database.washroom.DBWashroomDataAccessObject;
import entity.Building;
import interface_adapter.login.LoggedInViewModel;

/** Covers deterministic application state and resource facades without starting the application. */
class ApplicationStateCoverageTest {
    @Test
    void applicationModelsExposeSharedFeatureState() {
        final ApplicationModels models = new ApplicationModels();
        models.auth().loggedIn().setState(new LoggedInViewModel.State(true, "alice", true));
        assertEquals("alice", models.username());
        assertTrue(models.loggedIn());
        assertSame(models.auth(), models.auth());
        assertSame(models.reviews(), models.reviews());
        assertSame(models.features(), models.features());

        final FeatureModels features = models.features();
        assertSame(features.account(), features.account());
        assertSame(features.status(), features.status());
        assertSame(features.busyness(), features.busyness());
        assertSame(features.map(), features.map());
        assertSame(features.list(), features.list());
        assertSame(features.filter(), features.filter());
        assertSame(features.sortWashroom(), features.sortWashroom());

        final AuthModels auth = models.auth();
        assertSame(auth.isLoggedIn(), auth.isLoggedIn());
        assertSame(auth.login(), auth.login());
        assertSame(auth.loggedIn(), auth.loggedIn());
        final ReviewModels reviews = models.reviews();
        assertSame(reviews.reviews(), reviews.reviews());
        assertSame(reviews.writeReview(), reviews.writeReview());
        assertSame(reviews.reportReview(), reviews.reportReview());
        assertSame(reviews.moderate(), reviews.moderate());
    }

    @Test
    void deterministicCampusAndResourceFacadesExposeConfiguredValues() {
        final List<Building> locations = UniversityOfTorontoCampusLocations.coreLocations();
        assertEquals(4, locations.size());
        assertEquals("BA", locations.getFirst().code());
        assertTrue(!ApplicationNames.values().isEmpty());

        final GraphhopperRouteDataAccessObject routes = new GraphhopperRouteDataAccessObject("key");
        final GraphhopperGeocodingDataAccessObject geocoding = new GraphhopperGeocodingDataAccessObject("key");
        final RouteResources routeResources = new RouteResources(routes, geocoding);
        assertSame(routes, routeResources.routes());
        assertSame(geocoding, routeResources.geocoding());

        final MongoDatabase database = (MongoDatabase) Proxy.newProxyInstance(
            ApplicationStateCoverageTest.class.getClassLoader(), new Class<?>[] {MongoDatabase.class},
            (proxy, method, args) -> null);
        final DBDataAccessObject connection = new TestConnection(database);
        final DBWashroomDataAccessObject washrooms = new DBWashroomDataAccessObject(database);
        final DBReviewDataAccessObject reviews = new DBReviewDataAccessObject(database);
        final DBUserDataAccessObject users = new DBUserDataAccessObject(database);
        final DBStatusReportDataAccessObject reports = new DBStatusReportDataAccessObject(database);
        final DBEnrollmentDataAccessObject enrollment = new DBEnrollmentDataAccessObject(database);
        final DatabaseResources resources = new DatabaseResources(connection, washrooms, reviews, users, reports,
            enrollment);
        assertSame(connection, resources.connection());
        assertSame(washrooms, resources.washrooms());
        assertSame(reviews, resources.reviews());
        assertSame(users, resources.users());
        assertSame(reports, resources.reports());
        assertSame(enrollment, resources.enrollment());
    }

    private static final class TestConnection extends DBDataAccessObject {
        private TestConnection(final MongoDatabase database) {
            super(database);
        }
    }
}
