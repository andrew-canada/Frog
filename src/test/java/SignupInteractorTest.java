import entity.User;
import data_access.user.UserDataAccessInterface;
import use_case.signup.*;

import java.util.Optional;

final class SignupInteractorTest {
    static void run() {
        class Fake implements UserDataAccessInterface {
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

            public void setCurrentUser(User u) {
            }

            public Optional<User> getCurrentUser() {
                return Optional.empty();
            }

            public void removeUser(String n) {
            }
        }
        Fake fake = new Fake();
        final SignupOutputData[] out = new SignupOutputData[1];
        new SignupInteractor(fake, d -> out[0] = d).execute(new SignupInputData("new_user", "pass"));
        TestSupport.check(out[0].success() && fake.saved != null, "signup should save user");
        TestSupport.check(!fake.saved.passwordHash().equals("pass"), "password must be hashed");
    }
}
