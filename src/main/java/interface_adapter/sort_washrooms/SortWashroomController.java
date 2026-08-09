package interface_adapter.sort_washrooms;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import use_case.sort_washrooms.SortWashroomInputData;
import use_case.sort_washrooms.SortWashroomsInputBoundary;
import use_case.sort_washrooms.WashroomSortOrder;

public final class SortWashroomController {
    private final SortWashroomsInputBoundary interactor;

    public SortWashroomController(final SortWashroomsInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(final String sortBy, final List<String> washroomIdList, final double latitude, final double longitude) {
        CompletableFuture.runAsync(() -> interactor.execute(new SortWashroomInputData(
            WashroomSortOrder.fromDisplayLabel(sortBy), washroomIdList, latitude, longitude)));
    }
}
