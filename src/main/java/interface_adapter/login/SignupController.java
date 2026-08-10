package interface_adapter.login;

import use_case.signup.SignupInputBoundary;
import use_case.signup.SignupInputData;

public final class SignupController {
    private final SignupInputBoundary interactor;

    public SignupController(final SignupInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Performs this operation.
     *
     * @param username parameter value.
     *
     * @param password parameter value.
     */
    public void execute(final String username, final String password) {
        interactor.execute(new SignupInputData(username, password));
    }
}
