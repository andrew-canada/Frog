import database.security.BcryptPasswordHasher;
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
            public Optional<User> get(final String n) {
                return Optional.empty();
            }

            @Override
            public boolean existsByName(final String n) {
                return false;
            }

            @Override
            public void save(final User u) {
                saved = u;
            }

            @Override
            public Optional<User> currentUser() {
                return Optional.empty();
            }

            @Override
            public void setCurrentUser(final User u) {
            }

            @Override
            public void clear() {
            }

            @Override
            public void removeUser(final String n) {
            }
        }
        final Fake fake = new Fake();
        final SignupOutputData[] out = new SignupOutputData[1];
        new SignupInteractor(fake, fake, new BcryptPasswordHasher(), d -> out[0] = d).execute(
            new SignupInputData("new_user", "pass"));
        TestSupport.check(out[0].success() && fake.saved != null, "signup should save user");
        TestSupport.check(!fake.saved
            .passwordHash()
            .equals("pass"), "password must be hashed");
    }
}
