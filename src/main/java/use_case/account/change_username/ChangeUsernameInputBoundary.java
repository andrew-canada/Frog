package use_case.account.change_username;

public interface ChangeUsernameInputBoundary {

    /**
     * Performs this operation.
     *
     * @param inputData parameter value.
     */
    void execute(ChangeUsernameInputData inputData);

}
