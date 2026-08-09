package use_case.account.change_username;

import use_case.port.UserRepository;
import use_case.port.CurrentUserSession;
import entity.User;

public final class ChangeUsernameInteractor implements ChangeUsernameInputBoundary {

    private final UserRepository users;
    private final CurrentUserSession session;
    private final ChangeUsernameOutputBoundary presenter;

    public ChangeUsernameInteractor(UserRepository users, CurrentUserSession session,
                                    ChangeUsernameOutputBoundary presenter) {
        this.users = users;
        this.session = session;
        this.presenter = presenter;
    }

    @Override
    public void execute(ChangeUsernameInputData input) {

        User user = session.currentUser().orElse(null);

        if (user == null) {
            presenter.present(new ChangeUsernameOutputData(false, "Not logged in", null));
        } else if (users.get(input.newUsername()).isPresent()) {
            presenter.present(new ChangeUsernameOutputData(false, "Username already exists", user.username()));
        } else {
            users.removeUser(user.username());
            User newUser = new User(input.newUsername(), user.passwordHash(), user.personalPlan());
            users.save(newUser);
            session.setCurrentUser(newUser);
            presenter.present(new ChangeUsernameOutputData(true, "Changed username to " + input.newUsername(), input.newUsername()));
        }

    }

}
