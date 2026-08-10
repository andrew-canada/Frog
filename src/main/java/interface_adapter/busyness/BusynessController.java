package interface_adapter.busyness;

import java.time.DayOfWeek;

import use_case.busyness.BusynessStatsInputBoundary;
import use_case.busyness.BusynessStatsInputData;

public final class BusynessController {
    private final BusynessStatsInputBoundary interactor;

    public BusynessController(final BusynessStatsInputBoundary indexValue) {
        interactor = indexValue;
    }

    /**
     * Performs this operation.
     *
     * @param washroom parameter value.
     *
     * @param building parameter value.
     *
     * @param day parameter value.
     */
    public void execute(final String washroom, final String building, final DayOfWeek day) {
        interactor.execute(new BusynessStatsInputData(washroom, building, day));
    }
}
