import data_access.user.UserDataAccessInterface;
import entity.User;
import org.mindrot.jbcrypt.BCrypt;
import use_case.login.LoginInputData;
import use_case.login.LoginInteractor;
import use_case.login.LoginOutputData;

import java.util.Optional;

final class LoginInteractorTest {
    static void run() {
        class Fake implements UserDataAccessInterface {
            User current;
            final User saved = new User("demo", BCrypt.hashpw("secret", BCrypt.gensalt()), "");

            public Optional<User> get(String n) {
                return Optional.of(saved);
            }

            public boolean existsByName(String n) {
                return true;
            }

            public void save(User u) {
            }

            public Optional<User> getCurrentUser() {
                return Optional.ofNullable(current);
            }            public void setCurrentUser(User u) {
                current = u;
            }

            public void removeUser(String n) {
            }


        }
        Fake fake = new Fake();
        final LoginOutputData[] out = new LoginOutputData[1];
        new LoginInteractor(fake, d -> out[0] = d).execute(new LoginInputData("demo", "secret"));
        TestSupport.check(out[0].success() && fake.current != null, "valid login should set current user");
        class LegacyFake implements UserDataAccessInterface {
            User current;
            User saved = new User("legacy", "secret", "");

            public Optional<User> get(String n) {
                return Optional.of(saved);
            }

            public boolean existsByName(String n) {
                return true;
            }

            public void save(User u) {
                saved = u;
            }

            public Optional<User> getCurrentUser() {
                return Optional.ofNullable(current);
            }            public void setCurrentUser(User u) {
                current = u;
            }

            public void removeUser(String n) {
            }


        }
        LegacyFake legacy = new LegacyFake();
        new LoginInteractor(legacy, d -> out[0] = d).execute(new LoginInputData("legacy", "secret"));
        TestSupport.check(out[0].success() && BCrypt.checkpw("secret", legacy.saved.passwordHash()), "a correct legacy password should be upgraded to BCrypt");
    }
}
