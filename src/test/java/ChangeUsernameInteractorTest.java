import org.mindrot.jbcrypt.BCrypt;

import entity.User;

import java.util.HashMap;
import java.util.Optional;

import use_case.account.change_username.ChangeUsernameInputData;
import use_case.account.change_username.ChangeUsernameInteractor;
import use_case.account.change_username.ChangeUsernameOutputData;
import use_case.port.CurrentUserSession;
import use_case.port.UserRepository;

final class ChangeUsernameInteractorTest {
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
        final Fake fake = new Fake();
        User u = new User("demo", BCrypt.hashpw("secret", BCrypt.gensalt()), "");
        fake.save(u);
        fake.setCurrentUser(u);

        final ChangeUsernameOutputData[] out = new ChangeUsernameOutputData[1];
        new ChangeUsernameInteractor(fake, fake, d -> {out[0] = d;}).execute(new ChangeUsernameInputData("test"));

        TestSupport.check(out[0].success() &&
                (out[0].message().equals("Changed username to test") && out[0].username().equals("test")) &&
                (fake.current.username().equals("test") && BCrypt.checkpw("secret", fake.current.passwordHash()) && fake.current.personalPlan().isEmpty()) &&
                fake.get("demo").isEmpty() && fake.get("test").isPresent(),
                "Error with valid instance of change username");

        fake.save(new User("demo", BCrypt.hashpw("secret1", BCrypt.gensalt()), "a"));
        new ChangeUsernameInteractor(fake, fake, d -> {out[0] = d;}).execute(new ChangeUsernameInputData("demo"));
        TestSupport.check(!out[0].success() &&
                        (out[0].message().equals("Username already exists") && out[0].username().equals("test")) &&
                        (fake.current.username().equals("test") && BCrypt.checkpw("secret", fake.current.passwordHash()) && fake.current.personalPlan().isEmpty()) &&
                        fake.get("demo").isPresent() && fake.get("test").isPresent(),
                "Error with invalid instance of change username");
    }
}

