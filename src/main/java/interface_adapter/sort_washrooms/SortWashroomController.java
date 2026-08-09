package interface_adapter.sort_washrooms;

import use_case.sort_washrooms.SortWashroomInputData;
import use_case.sort_washrooms.SortWashroomsInputBoundary;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class SortWashroomController {
    private final SortWashroomsInputBoundary interactor;

    public SortWashroomController(SortWashroomsInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String sortBy, List<String> washroomIdList, double latitude, double longitude) {
        CompletableFuture.runAsync(() -> interactor.execute(new SortWashroomInputData(sortBy, washroomIdList, latitude, longitude)));
    }
}
