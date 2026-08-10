package use_case.filter;

public interface FilterOutputBoundary {
    /**
     * Performs this operation.
     *
     * @param outputData parameter value.
     */
    void present(FilterOutputData outputData);

    /**
     * Performs this operation.
     *
     * @param message parameter value.
     */
    void presentError(String message);
}
