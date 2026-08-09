package use_case.sort_washrooms;

import entity.Washroom;
import java.util.ArrayList;
import java.util.Comparator;
import use_case.port.WashroomRepository;

public class SortWashroomInteractor implements SortWashroomsInputBoundary {
    private final WashroomRepository washroomDAO;
    private final SortWashroomsOutputBoundary presenter;

    public SortWashroomInteractor(final WashroomRepository washroomDAO,
                                  final SortWashroomsOutputBoundary presenter) {
        this.washroomDAO = washroomDAO;
        this.presenter = presenter;
    }

    private static double distance(final double a, final double b, final double c, final double d) {
        final double x = Math.toRadians(d - b) * Math.cos(Math.toRadians((a + c) / 2));
        final double y = Math.toRadians(c - a);
        return Math.sqrt(x * x + y * y) * 6_371_000;
    }

    @Override
    public void execute(final SortWashroomInputData inputData) {
        final Comparator<entity.Washroom> comparator;
        final WashroomSortOrder sortOrder = inputData.sortOrder() == null
            ? WashroomSortOrder.ALPHABETICAL : inputData.sortOrder();
        comparator = switch (sortOrder) {
            case HIGHEST_RATED -> Comparator
                .comparing(
                    (entity.Washroom washroom) -> washroom
                        .reviewSummary()
                        .averageRating())
                .reversed();
            case NEAREST -> Comparator.comparing(
                (entity.Washroom washroom) -> distance(inputData.lat(), inputData.lng(),
                    washroom
                        .building()
                        .latitude(), washroom
                        .building()
                        .longitude()));
            case ALPHABETICAL -> Comparator
                .comparing(
                    (entity.Washroom washroom) -> washroom
                        .building()
                        .name(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entity.Washroom::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entity.Washroom::id);
        };
        final ArrayList<Washroom> sortedWashroom = new ArrayList<>(washroomDAO.getByIds(inputData.washroomIdList()));
        sortedWashroom.sort(comparator);
        presenter.present(new SortWashroomsOutputData(
            true,
            sortedWashroom,
            inputData.lat(),
            inputData.lng()));
    }

}
