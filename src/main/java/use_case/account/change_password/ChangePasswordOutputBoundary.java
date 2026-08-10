package use_case.account.change_password;

public interface ChangePasswordOutputBoundary {

    /**
     * Performs this operation.
     *
     * @param outputData parameter value.
     */
    void present(ChangePasswordOutputData outputData);

}
