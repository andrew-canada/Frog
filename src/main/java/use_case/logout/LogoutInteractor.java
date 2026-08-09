package use_case.logout;

import use_case.port.CurrentUserSession;

public final class LogoutInteractor implements LogoutInputBoundary {

    private final CurrentUserSession session;
    private final LogoutOutputBoundary presenter;

    public LogoutInteractor(CurrentUserSession session, LogoutOutputBoundary presenter) {
        this.session = session;
        this.presenter = presenter;
    }

    @Override
    public void execute() {

        session.clear();
        presenter.present();

    }

}
