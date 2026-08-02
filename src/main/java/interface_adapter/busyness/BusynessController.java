package interface_adapter.busyness;

import use_case.busyness.BusynessStatsInputBoundary;
import use_case.busyness.BusynessStatsInputData;

import java.time.DayOfWeek;

public final class BusynessController {
    private final BusynessStatsInputBoundary interactor;

    public BusynessController(BusynessStatsInputBoundary i) {
        interactor = i;
    }

    public void execute(String washroom, String building, DayOfWeek day) {
        interactor.execute(new BusynessStatsInputData(washroom, building, day));
    }
}
