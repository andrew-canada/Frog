package use_case.signup;

public interface SignupInputBoundary {
    /**
     * Performs this operation.
     *
     * @param inputData parameter value.
     */
    void execute(SignupInputData inputData);
}
