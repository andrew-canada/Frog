package use_case.account.load_account;

public interface LoadAccountOutputBoundary {

    /**
     * Performs this operation.
     *
     * @param outputData parameter value.
     */
    void present(LoadAccountOutputData outputData);

}
