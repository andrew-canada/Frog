package use_case.account.delete_account;

public interface DeleteAccountOutputBoundary {

    /**
     * Performs this operation.
     *
     * @param outputData parameter value.
     */
    void present(DeleteAccountOutputData outputData);

}
