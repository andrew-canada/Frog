package use_case.login;

public interface LoginOutputBoundary {
    /**
     * Performs this operation.
     *
     * @param outputData parameter value.
     */
    void present(LoginOutputData outputData);
}
