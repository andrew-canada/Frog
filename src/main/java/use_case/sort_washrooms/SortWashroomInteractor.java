package use_case.sort_washrooms;

import java.util.ArrayList;
import java.util.Comparator;

import entity.Washroom;
import use_case.port.WashroomRepository;

public class SortWashroomInteractor implements SortWashroomsInputBoundary {
    private static final int EARTH_RADIUS_METERS = 6_371_000;
    private final WashroomRepository washroomRepository;
    private final SortWashroomsOutputBoundary presenter;

    public SortWashroomInteractor(final WashroomRepository washroomRepository,
                                  final SortWashroomsOutputBoundary presenter) {
        this.washroomRepository = washroomRepository;
        this.presenter = presenter;
    }

    private static double distance(final double firstValue, final double secondValue, final double thirdValue,
        final double fourthValue) {
        final double averageLatitude = (firstValue + thirdValue) / 2;
        final double x = Math.toRadians(fourthValue - secondValue) * Math.cos(Math.toRadians(averageLatitude));
        final double y = Math.toRadians(thirdValue - firstValue);
        return Math.sqrt(x * x + y * y) * EARTH_RADIUS_METERS;
    }

    @Override
    public void execute(final SortWashroomInputData inputData) {
        final Comparator<entity.Washroom> comparator;
        final WashroomSortOrder sortOrder;
        if (inputData.sortOrder() == null) {
            sortOrder = WashroomSortOrder.ALPHABETICAL;
        }
        else {
            sortOrder = inputData.sortOrder();
        }
        comparator = switch (sortOrder) {
            case HIGHEST_RATED -> Comparator
                .comparing((entity.Washroom washroom) -> {
                    return washroom
                        .reviewSummary()
                        .averageRating();
                })
                .reversed();
            case NEAREST -> Comparator.comparing(
                (entity.Washroom washroom) -> {
                    return distance(inputData.lat(), inputData.lng(), washroom
                        .building()
                        .latitude(), washroom
                        .building()
                        .longitude());
                });
            case ALPHABETICAL -> Comparator
                .comparing((entity.Washroom washroom) -> {
                    return washroom
                        .building()
                        .name();
                }, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entity.Washroom::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entity.Washroom::id);
        };
        final ArrayList<Washroom> sortedWashroom = new ArrayList<>(washroomRepository
            .getByIds(inputData.washroomIdList()));
        sortedWashroom.sort(comparator);
        presenter.present(new SortWashroomsOutputData(true, sortedWashroom, inputData.lat(), inputData.lng()));
    }

}
