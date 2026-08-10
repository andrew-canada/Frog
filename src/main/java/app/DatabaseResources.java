package app;

import database.DBDataAccessObject;
import database.enrollment.DBEnrollmentDataAccessObject;
import database.review.DBReviewDataAccessObject;
import database.status.DBStatusReportDataAccessObject;
import database.user.DBUserDataAccessObject;
import database.washroom.DBWashroomDataAccessObject;

/** MongoDB resources shared by the application. */
final class DatabaseResources {
    private final DBDataAccessObject connection;
    private final DBWashroomDataAccessObject washrooms;
    private final DBReviewDataAccessObject reviews;
    private final DBUserDataAccessObject users;
    private final DBStatusReportDataAccessObject reports;
    private final DBEnrollmentDataAccessObject enrollment;

    DatabaseResources(final DBDataAccessObject connection, final DBWashroomDataAccessObject washrooms,
                      final DBReviewDataAccessObject reviews, final DBUserDataAccessObject users,
                      final DBStatusReportDataAccessObject reports, final DBEnrollmentDataAccessObject enrollment) {
        this.connection = connection;
        this.washrooms = washrooms;
        this.reviews = reviews;
        this.users = users;
        this.reports = reports;
        this.enrollment = enrollment;
    }

    DBDataAccessObject connection() {
        return connection;
    }

    DBWashroomDataAccessObject washrooms() {
        return washrooms;
    }

    DBReviewDataAccessObject reviews() {
        return reviews;
    }

    DBUserDataAccessObject users() {
        return users;
    }

    DBStatusReportDataAccessObject reports() {
        return reports;
    }

    DBEnrollmentDataAccessObject enrollment() {
        return enrollment;
    }
}
