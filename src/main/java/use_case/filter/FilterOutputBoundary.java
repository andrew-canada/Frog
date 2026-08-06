package use_case.filter;

public interface FilterOutputBoundary {
    void present(FilterOutputData outputData);

    void presentError(String message);
}
