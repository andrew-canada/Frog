import org.mindrot.jbcrypt.BCrypt;

import entity.User;

import java.util.HashMap;
import java.util.Optional;

import use_case.account.change_password.ChangePasswordInputData;
import use_case.account.change_password.ChangePasswordInteractor;
import use_case.account.change_password.ChangePasswordOutputData;
import use_case.port.CurrentUserSession;
import use_case.port.PasswordHasher;
import use_case.port.UserRepository;

final class ChangePasswordInteractorTest {
    static void run() {
        class Fake implements UserRepository, CurrentUserSession {
            User current;
            final HashMap<String, User> users = new HashMap<>();

            @Override
            public Optional<User> get(final String n) {
                if (users.containsKey(n)) {
                    return Optional.of(users.get(n));
                } else {
                    return Optional.empty();
                }
            }

            @Override
            public boolean existsByName(final String n) {
                return users.containsKey(n);
            }

            @Override
            public void save(final User u) {
                users.put(u.username(), u);
            }

            @Override
            public Optional<User> currentUser() {
                return Optional.ofNullable(current);
            }

            @Override
            public void setCurrentUser(final User u) {
                current = u;
            }

            @Override
            public void clear() {
                current = null;
            }

            @Override
            public void removeUser(final String n) {
                users.remove(n);
            }

        }

        class FakePassword implements PasswordHasher {

            @Override
            public String hash(String password) {
                return BCrypt.hashpw(password, BCrypt.gensalt());
            }

            public boolean matches(String password, String storedHash) {
                return BCrypt.checkpw(password, storedHash);
            }

            public boolean isCurrentHash(String storedHash) {
                return false;
            }

        }

        final Fake fake = new Fake();
        User u = new User("demo", BCrypt.hashpw("secret", BCrypt.gensalt()), "");
        fake.save(u);
        fake.setCurrentUser(u);

        final FakePassword fakePassword = new FakePassword();

        final ChangePasswordOutputData[] out = new ChangePasswordOutputData[1];
        new ChangePasswordInteractor(fake, fake, fakePassword, d -> {out[0] = d;}).execute(new ChangePasswordInputData("test", "test"));

        TestSupport.check(out[0].success() && out[0].message().equals("Changed password") &&
                        (fake.current.username().equals("demo") && fakePassword.matches("test", fake.current.passwordHash()) && fake.current.personalPlan().isEmpty()) &&
                        fake.get("demo").isPresent(),
                "Error with valid instance of change password");

        new ChangePasswordInteractor(fake, fake, fakePassword, d -> {out[0] = d;}).execute(new ChangePasswordInputData("t", "t"));
        TestSupport.check(!out[0].success() && out[0].message().equals("Password needs 4+ characters") &&
                        (fake.current.username().equals("demo") && fakePassword.matches("test", fake.current.passwordHash()) && fake.current.personalPlan().isEmpty()) &&
                        fake.get("demo").isPresent(),
                "Error with invalid instance of change password where password is too short");

        new ChangePasswordInteractor(fake, fake, fakePassword, d -> {out[0] = d;}).execute(new ChangePasswordInputData("testing", "Testing"));
        TestSupport.check(!out[0].success() && out[0].message().equals("Passwords do not match") &&
                        (fake.current.username().equals("demo") && fakePassword.matches("test", fake.current.passwordHash()) && fake.current.personalPlan().isEmpty()) &&
                        fake.get("demo").isPresent(),
                "Error with invalid instance of change password where password does not match confirm password");
    }
}