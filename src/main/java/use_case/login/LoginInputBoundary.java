package use_case.login;

public interface LoginInputBoundary {
    /**
     * Performs this operation.
     *
     * @param inputData parameter value.
     */
    void execute(LoginInputData inputData);
}
