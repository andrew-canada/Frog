package interface_adapter.account.delete_account;

import use_case.account.delete_account.DeleteAccountInputBoundary;

public final class DeleteAccountController {

    private final DeleteAccountInputBoundary interactor;

    public DeleteAccountController(final DeleteAccountInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Performs this operation.
     */
    public void execute() {
        interactor.execute();
    }

}
