import data_access.security.BCryptPasswordHasher;
import use_case.port.UserRepository;
import use_case.port.CurrentUserSession;
import entity.User;
import use_case.signup.SignupInputData;
import use_case.signup.SignupInteractor;
import use_case.signup.SignupOutputData;

import java.util.Optional;

final class SignupInteractorTest {
    static void run() {
        class Fake implements UserRepository, CurrentUserSession {
            User saved;

            public Optional<User> get(String n) {
                return Optional.empty();
            }

            public boolean existsByName(String n) {
                return false;
            }

            public void save(User u) {
                saved = u;
            }

            public Optional<User> currentUser() {
                return Optional.empty();
            }

            public void setCurrentUser(User u) {
            }

            public void clear() {
            }

            public void removeUser(String n) {
            }
        }
        Fake fake = new Fake();
        final SignupOutputData[] out = new SignupOutputData[1];
        new SignupInteractor(fake, fake, new BCryptPasswordHasher(), d -> out[0] = d).execute(new SignupInputData("new_user", "pass"));
        TestSupport.check(out[0].success() && fake.saved != null, "signup should save user");
        TestSupport.check(!fake.saved.passwordHash().equals("pass"), "password must be hashed");
    }
}
