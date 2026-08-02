package use_case.account.change_username;

import entity.User;
import use_case.gateway.UserDataAccessInterface;

public final class ChangeUsernameInteractor implements ChangeUsernameInputBoundary {

    private final UserDataAccessInterface users;
    private final ChangeUsernameOutputBoundary presenter;

    public ChangeUsernameInteractor(UserDataAccessInterface users, ChangeUsernameOutputBoundary presenter) {
        this.users = users;
        this.presenter = presenter;
    }

    @Override
    public void execute(ChangeUsernameInputData input) {

        User user = users.getCurrentUser().orElse(null);

        if (user == null) {
            presenter.present(new ChangeUsernameOutputData(false, "Not logged in", null));
        } else if (users.get(input.newUsername()).isPresent()) {
            presenter.present(new ChangeUsernameOutputData(false, "Username already exists", user.username()));
        } else {
            users.removeUser(user.username());
            User newUser = new User(input.newUsername(), user.passwordHash(), user.personalPlan());
            users.save(newUser);
            users.setCurrentUser(newUser);
            presenter.present(new ChangeUsernameOutputData(true, "Changed username to " + input.newUsername(), input.newUsername()));
        }

    }

}
