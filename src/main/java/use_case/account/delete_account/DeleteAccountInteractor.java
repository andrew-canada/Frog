package use_case.account.delete_account;

import entity.User;
import use_case.port.CurrentUserSession;
import use_case.port.UserRepository;

public final class DeleteAccountInteractor implements DeleteAccountInputBoundary {

    private final UserRepository users;
    private final CurrentUserSession session;
    private final DeleteAccountOutputBoundary presenter;

    public DeleteAccountInteractor(final UserRepository users, final CurrentUserSession session,
                                   final DeleteAccountOutputBoundary presenter) {
        this.users = users;
        this.session = session;
        this.presenter = presenter;
    }

    @Override
    public void execute() {

        final User user = session
            .currentUser()
            .orElse(null);

        if (user == null) {
            presenter.present(new DeleteAccountOutputData(false, "You are not logged in"));
        }
        else {
            users.removeUser(user.username());
            session.clear();
            presenter.present(new DeleteAccountOutputData(true, ""));
        }

    }

}
