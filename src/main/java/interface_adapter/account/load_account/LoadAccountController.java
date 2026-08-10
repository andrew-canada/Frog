package interface_adapter.account.load_account;

import use_case.account.load_account.LoadAccountInputBoundary;

public final class LoadAccountController {

    private final LoadAccountInputBoundary interactor;

    public LoadAccountController(final LoadAccountInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Performs this operation.
     */
    public void execute() {
        interactor.execute();
    }

}
