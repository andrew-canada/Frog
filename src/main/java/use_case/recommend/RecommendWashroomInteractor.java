package use_case.recommend;

import entity.StatusReport;
import entity.Washroom;
import use_case.gateway.StatusReportDataAccessInterface;
import use_case.gateway.WashroomDataAccessInterface;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RecommendWashroomInteractor implements RecommendWashroomInputBoundary {
    private final WashroomDataAccessInterface washrooms;
    private final StatusReportDataAccessInterface reports;
    private final RecommendWashroomOutputBoundary presenter;
    public RecommendWashroomInteractor(WashroomDataAccessInterface washrooms,
            StatusReportDataAccessInterface reports, RecommendWashroomOutputBoundary presenter) {
        this.washrooms = washrooms; this.reports = reports; this.presenter = presenter;
    }
    @Override public void execute(RecommendWashroomInputData in) {
        List<Washroom> candidates = washrooms.getNearby(in.latitude(), in.longitude(), 2_000).stream()
                .filter(w -> !in.accessibleOnly() || w.accessible())
                .filter(w -> in.gender() == null || w.gender() == in.gender() || w.gender() == Washroom.Gender.ALL_GENDER)
                .toList();
        if (candidates.isEmpty()) { presenter.presentError("No washrooms match those preferences"); return; }
        Scored best = candidates.stream().map(w -> score(w, in)).max((a, b) -> Double.compare(a.total, b.total)).orElseThrow();
        presenter.present(new RecommendWashroomOutputData(best.w.id(), best.w.name(),
                best.w.reviewSummary().averageRating(), best.distance, best.busyness, best.w.accessible(),
                best.w.gender().name().replace('_', '-').toLowerCase(), best.total, best.parts));
    }
    private Scored score(Washroom w, RecommendWashroomInputData in) {
        int distance = (int) Math.round(distance(in.latitude(), in.longitude(), w.building().latitude(), w.building().longitude()));
        List<StatusReport> recent = reports.getRecentForWashroom(w.id(), LocalDateTime.now().minusHours(4));
        double busy = recent.stream().mapToInt(StatusReport::busyness).average().orElse(0);
        double rating = w.reviewSummary().averageRating() / 5.0;
        double proximity = Math.max(0, 1 - distance / 2000.0);
        double quiet = recent.isEmpty() ? 0 : 1 - busy / 5.0;
        double match = (!in.accessibleOnly() || w.accessible()) ? 1 : 0;
        double wr = in.inAHurry() ? .20 : .40, wp = in.inAHurry() ? .45 : .25,
                wq = in.inAHurry() ? .25 : .20, wm = in.inAHurry() ? .10 : .15;
        Map<String, Double> parts = new LinkedHashMap<>();
        parts.put("rating", rating * wr); parts.put("proximity", proximity * wp);
        parts.put("quiet now", quiet * wq); parts.put("preference match", match * wm);
        return new Scored(w, distance, busy, parts.values().stream().mapToDouble(Double::doubleValue).sum(), parts);
    }
    private static double distance(double a, double b, double c, double d) {
        double x = Math.toRadians(d-b)*Math.cos(Math.toRadians((a+c)/2)); double y = Math.toRadians(c-a);
        return Math.sqrt(x*x+y*y)*6_371_000;
    }
    private record Scored(Washroom w, int distance, double busyness, double total, Map<String, Double> parts) { }
}
