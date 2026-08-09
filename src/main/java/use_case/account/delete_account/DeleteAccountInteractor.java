package use_case.account.delete_account;

import use_case.port.UserRepository;
import use_case.port.CurrentUserSession;
import entity.User;

public final class DeleteAccountInteractor implements DeleteAccountInputBoundary {

    private final UserRepository users;
    private final CurrentUserSession session;
    private final DeleteAccountOutputBoundary presenter;

    public DeleteAccountInteractor(UserRepository users, CurrentUserSession session,
                                   DeleteAccountOutputBoundary presenter) {
        this.users = users;
        this.session = session;
        this.presenter = presenter;
    }

    @Override
    public void execute() {

        User user = session.currentUser().orElse(null);

        if (user == null) {
            presenter.present(new DeleteAccountOutputData(false, "You are not logged in"));
        } else {
            users.removeUser(user.username());
            session.clear();
            presenter.present(new DeleteAccountOutputData(true, ""));
        }

    }

}
