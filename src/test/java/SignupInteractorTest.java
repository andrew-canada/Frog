import database.security.BCryptPasswordHasher;
import entity.User;
import java.util.Optional;
import use_case.port.CurrentUserSession;
import use_case.port.UserRepository;
import use_case.signup.SignupInputData;
import use_case.signup.SignupInteractor;
import use_case.signup.SignupOutputData;

final class SignupInteractorTest {
    static void run() {
        class Fake implements UserRepository, CurrentUserSession {
            User saved;

            @Override
            @Override
            public Optional<User> get(final String n) {
                return Optional.empty();
            }

            @Override
            @Override
            public boolean existsByName(final String n) {
                return false;
            }

            @Override
            @Override
            public void save(final User u) {
                saved = u;
            }

            @Override
            @Override
            public Optional<User> currentUser() {
                return Optional.empty();
            }

            @Override
            @Override
            public void setCurrentUser(final User u) {
            }

            @Override
            @Override
            public void clear() {
            }

            @Override
            @Override
            public void removeUser(final String n) {
            }
        }
        final Fake fake = new Fake();
        final SignupOutputData[] out = new SignupOutputData[1];
        new SignupInteractor(fake, fake, new BCryptPasswordHasher(), d -> out[0] = d).execute(
            new SignupInputData("new_user", "pass"));
        TestSupport.check(out[0].success() && fake.saved != null, "signup should save user");
        TestSupport.check(!fake.saved
            .passwordHash()
            .equals("pass"), "password must be hashed");
    }
}
