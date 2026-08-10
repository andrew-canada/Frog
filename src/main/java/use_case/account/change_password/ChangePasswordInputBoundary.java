package use_case.account.change_password;

public interface ChangePasswordInputBoundary {

    /**
     * Performs this operation.
     *
     * @param inputData parameter value.
     */
    void execute(ChangePasswordInputData inputData);

}
