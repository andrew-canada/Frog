package interface_adapter.busyness;

import java.time.DayOfWeek;
import use_case.busyness.BusynessStatsInputBoundary;
import use_case.busyness.BusynessStatsInputData;

public final class BusynessController {
    private final BusynessStatsInputBoundary interactor;

    public BusynessController(final BusynessStatsInputBoundary i) {
        interactor = i;
    }

    public void execute(final String washroom, final String building, final DayOfWeek day) {
        interactor.execute(new BusynessStatsInputData(washroom, building, day));
    }
}
