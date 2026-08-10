package use_case.account.change_username;

public interface ChangeUsernameOutputBoundary {

    /**
     * Performs this operation.
     *
     * @param outputData parameter value.
     */
    void present(ChangeUsernameOutputData outputData);

}
