import org.mindrot.jbcrypt.BCrypt;

import database.security.BCryptPasswordHasher;
import entity.User;
import interface_adapter.account.IsLoggedInViewModel;
import interface_adapter.login.LoggedInViewModel;
import interface_adapter.login.LoginViewModel;
import interface_adapter.logout.LogoutPresenter;
import java.util.Optional;
import use_case.login.LoginInputData;
import use_case.login.LoginInteractor;
import use_case.login.LoginOutputData;
import use_case.port.CurrentUserSession;
import use_case.port.UserRepository;

final class LoginInteractorTest {
    static void run() {
        class Fake implements UserRepository, CurrentUserSession {
            User current;
            final User saved = new User("demo", BCrypt.hashpw("secret", BCrypt.gensalt()), "");

            public Optional<User> get(final String n) {
                return Optional.of(saved);
            }

            public boolean existsByName(final String n) {
                return true;
            }

            public void save(final User u) {
            }

            public Optional<User> currentUser() {
                return Optional.ofNullable(current);
            }

            public void setCurrentUser(final User u) {
                current = u;
            }

            public void clear() {
                current = null;
            }

            public void removeUser(final String n) {
            }


        }
        final Fake fake = new Fake();
        final LoginOutputData[] out = new LoginOutputData[1];
        new LoginInteractor(fake, fake, new BCryptPasswordHasher(), d -> out[0] = d).execute(
            new LoginInputData("demo", "secret"));
        TestSupport.check(out[0].success() && fake.current != null, "valid login should set current user");
        class LegacyFake implements UserRepository, CurrentUserSession {
            User current;
            User saved = new User("legacy", "secret", "");

            public Optional<User> get(final String n) {
                return Optional.of(saved);
            }

            public boolean existsByName(final String n) {
                return true;
            }

            public void save(final User u) {
                saved = u;
            }

            public Optional<User> currentUser() {
                return Optional.ofNullable(current);
            }

            public void setCurrentUser(final User u) {
                current = u;
            }

            public void clear() {
                current = null;
            }

            public void removeUser(final String n) {
            }


        }
        final LegacyFake legacy = new LegacyFake();
        new LoginInteractor(legacy, legacy, new BCryptPasswordHasher(), d -> out[0] = d).execute(
            new LoginInputData("legacy", "secret"));
        TestSupport.check(out[0].success() && BCrypt.checkpw("secret", legacy.saved.passwordHash()),
            "a correct legacy password should be upgraded to BCrypt");
        logoutClearsLoginPresentationState();
    }

    private static void logoutClearsLoginPresentationState() {
        final LoginViewModel login = new LoginViewModel();
        final LoggedInViewModel loggedIn = new LoggedInViewModel();
        final IsLoggedInViewModel session = new IsLoggedInViewModel();
        login.setState(new LoginViewModel.State(true, "demo", "Welcome back, demo"));
        loggedIn.setState(new LoggedInViewModel.State(true, "demo", false));
        session
            .getState()
            .setIsLoggedIn(true);
        session
            .getState()
            .setUsername("demo");

        new LogoutPresenter(session, login, loggedIn).present();

        TestSupport.check(!login
                .getState()
                .success() && login
                .getState()
                .message()
                .isBlank(),
            "logout should clear the previous welcome message");
        TestSupport.check(!loggedIn
                .getState()
                .loggedIn() && loggedIn
                .getState()
                .username()
                .equals("Guest"),
            "logout should clear the active user");
    }
}
