package use_case.account.load_account;

import entity.User;
import use_case.port.CurrentUserSession;

public final class LoadAccountInteractor implements LoadAccountInputBoundary {

    private final CurrentUserSession session;
    private final LoadAccountOutputBoundary presenter;

    public LoadAccountInteractor(final CurrentUserSession session,
                                   final LoadAccountOutputBoundary presenter) {
        this.session = session;
        this.presenter = presenter;
    }

    @Override
    public void execute() {

        final User user = session
                .currentUser()
                .orElse(null);

        if (user == null) {
            presenter.present(new LoadAccountOutputData("", ""));
        }
        else {
            presenter.present(new LoadAccountOutputData(user.username(), user.personalPlan()));
        }

    }

}
