package app;

import database.review.DBReviewDataAccessObject;
import database.status.DBStatusReportDataAccessObject;
import database.washroom.DBWashroomDataAccessObject;
import entity.Washroom;
import views.MainView;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Startup and map-data work kept outside the dependency-wiring composition root. */
final class CampusStartup {
    private CampusStartup() {
    }

    static Set<String> loadWashroomNames() {
        InputStream input = CampusStartup.class.getResourceAsStream("/data/washrooms.json");
        if (input == null) throw new IllegalStateException("Missing data/washrooms.json resource.");
        try (input; JsonReader reader = Json.createReader(input)) {
            return Set.copyOf(reader.readArray().stream()
                    .map(value -> ((JsonObject) value).getString("name")).toList());
        } catch (Exception failure) {
            throw new IllegalStateException("Could not load data/washrooms.json.", failure);
        }
    }

    static void seedInitialData(DBWashroomDataAccessObject washrooms, DBReviewDataAccessObject reviews,
                                DBStatusReportDataAccessObject reports, Set<String> names) {
        washrooms.ensurePerformanceIndexes();
        reviews.ensurePerformanceIndexes();
        reports.ensurePerformanceIndexes();
        List<Washroom> jsonWashrooms = washrooms.getByNames(names);
        reviews.ensureJsonReviews(jsonWashrooms);
        reports.ensureJsonHourlyReports(jsonWashrooms);
    }

    static List<MainView.HeatmapData> heatmapData(List<Washroom> washrooms,
                                                   DBStatusReportDataAccessObject reports) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, entity.StatusReport> currentHour = reports.getCurrentHourForWashrooms(
                washrooms.stream().map(Washroom::id).toList(), now.getHour());
        return washrooms.stream().map(washroom -> Optional.ofNullable(currentHour.get(washroom.id()))
                .map(report -> new MainView.HeatmapData(washroom.id(), report.busyness(), report.cleanliness()))
                .orElseGet(() -> new MainView.HeatmapData(washroom.id(), Double.NaN, Double.NaN))).toList();
    }
}
