package use_case.filter;

public interface FilterInputBoundary {
    /**
     * Performs this operation.
     *
     * @param inputData parameter value.
     */
    void execute(FilterInputData inputData);
}
