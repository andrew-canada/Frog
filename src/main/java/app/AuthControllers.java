package app;

import database.security.BcryptPasswordHasher;
import interface_adapter.login.LoggedInViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.login.LoginViewModel;
import interface_adapter.login.SignupController;
import interface_adapter.login.SignupPresenter;
import interface_adapter.logout.LogoutController;
import interface_adapter.logout.LogoutPresenter;
import use_case.login.LoginInteractor;
import use_case.logout.LogoutInteractor;
import use_case.signup.SignupInteractor;

/** Authentication controller construction. */
final class AuthControllers {
    private final LoginController login;
    private final SignupController signup;
    private final LogoutController logout;

    private AuthControllers(final LoginController login, final SignupController signup,
                            final LogoutController logout) {
        this.login = login;
        this.signup = signup;
        this.logout = logout;
    }

    static AuthControllers create(final ApplicationInfrastructure infrastructure, final AuthModels models) {
        final BcryptPasswordHasher passwordHasher = new BcryptPasswordHasher();
        final LoginViewModel loginModel = models.login();
        final LoggedInViewModel loggedInModel = models.loggedIn();
        final LoginPresenter loginPresenter = new LoginPresenter(loginModel, loggedInModel, models.isLoggedIn());
        final SignupPresenter signupPresenter = new SignupPresenter(loginModel, loggedInModel);
        return new AuthControllers(
            new LoginController(new LoginInteractor(infrastructure.users(), infrastructure.users(), passwordHasher,
                loginPresenter)),
            new SignupController(new SignupInteractor(infrastructure.users(), infrastructure.users(), passwordHasher,
                signupPresenter)),
            new LogoutController(new LogoutInteractor(infrastructure.users(),
                new LogoutPresenter(models.isLoggedIn(), loginModel, loggedInModel))));
    }

    LoginController login() {
        return login;
    }

    SignupController signup() {
        return signup;
    }

    LogoutController logout() {
        return logout;
    }
}
