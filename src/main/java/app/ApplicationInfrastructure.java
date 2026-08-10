package app;

import java.util.Optional;

import database.DBDataAccessObject;
import database.enrollment.DBEnrollmentDataAccessObject;
import database.review.DBReviewDataAccessObject;
import database.route.GraphhopperGeocodingDataAccessObject;
import database.route.GraphhopperRouteDataAccessObject;
import database.status.DBStatusReportDataAccessObject;
import database.user.DBUserDataAccessObject;
import database.washroom.DBWashroomDataAccessObject;
import entity.Washroom;

/** Concrete external resources owned by the composition root. */
final class ApplicationInfrastructure {
    private final DatabaseResources database;
    private final RouteResources route;

    private ApplicationInfrastructure(final DatabaseResources database, final RouteResources route) {
        this.database = database;
        this.route = route;
    }

    static ApplicationInfrastructure create() {
        final String key = requiredEnvironment(GraphhopperRouteDataAccessObject.API_KEY_ENV);
        final DBDataAccessObject connection = DBDataAccessObject.fromEnvironment();
        final var database = connection.database();
        final DBWashroomDataAccessObject washrooms = new DBWashroomDataAccessObject(database);
        final DBReviewDataAccessObject reviews = new DBReviewDataAccessObject(database);
        final DBUserDataAccessObject users = new DBUserDataAccessObject(database);
        users.ensureModerator("frog");
        users.ensureModerator("sheena_q");
        final DatabaseResources databaseResources = new DatabaseResources(connection, washrooms, reviews, users,
            new DBStatusReportDataAccessObject(database), new DBEnrollmentDataAccessObject(database));
        final RouteResources routeResources = new RouteResources(
            new GraphhopperRouteDataAccessObject(key), new GraphhopperGeocodingDataAccessObject(key));
        return new ApplicationInfrastructure(databaseResources, routeResources);
    }

    private static String requiredEnvironment(final String name) {
        final String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set the " + name + " environment variable before starting FlushID.");
        }
        return value;
    }

    DBDataAccessObject connection() {
        return database.connection();
    }

    DBWashroomDataAccessObject washrooms() {
        return database.washrooms();
    }

    DBReviewDataAccessObject reviews() {
        return database.reviews();
    }

    DBUserDataAccessObject users() {
        return database.users();
    }

    DBStatusReportDataAccessObject reports() {
        return database.reports();
    }

    DBEnrollmentDataAccessObject enrollment() {
        return database.enrollment();
    }

    GraphhopperRouteDataAccessObject routes() {
        return route.routes();
    }

    GraphhopperGeocodingDataAccessObject geocoding() {
        return route.geocoding();
    }

    Optional<Washroom> selected(final String id) {
        final Optional<Washroom> result;
        if (id == null || id.isBlank()) {
            result = Optional.empty();
        }
        else {
            result = database.washrooms().getById(id);
        }
        return result;
    }
}
