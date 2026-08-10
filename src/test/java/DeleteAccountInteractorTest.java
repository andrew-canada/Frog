import org.mindrot.jbcrypt.BCrypt;

import entity.User;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import use_case.account.delete_account.DeleteAccountInteractor;
import use_case.account.delete_account.DeleteAccountOutputData;
import use_case.port.CurrentUserSession;
import use_case.port.UserRepository;

final class DeleteAccountInteractorTest {
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

        final DeleteAccountOutputData[] out = new DeleteAccountOutputData[1];
        new DeleteAccountInteractor(fake, fake, d -> {out[0] = d;}).execute();

        TestSupport.check(out[0].success() && out[0].message().isEmpty() && Objects.isNull(fake.current) && fake.get("demo").isEmpty(),
                "Error with valid instance of delete account");

    }
}