package use_case.busyness;

public interface BusynessStatsOutputBoundary {
    /**
     * Performs this operation.
     *
     * @param outputData parameter value.
     */
    void present(BusynessStatsOutputData outputData);
}
