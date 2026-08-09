package interface_adapter.sort_washrooms;

import use_case.sort_washrooms.SortWashroomInputData;
import use_case.sort_washrooms.SortWashroomsInputBoundary;

import java.util.concurrent.CompletableFuture;

public class SortWashroomController {
    private final SortWashroomsInputBoundary interactor;

    public SortWashroomController(SortWashroomsInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String sortBy, double latitude, double longitude) {
        SortWashroomInputData inputData = new SortWashroomInputData(sortBy,latitude, longitude);
        CompletableFuture.runAsync(() -> interactor.execute(inputData));
    }
}
