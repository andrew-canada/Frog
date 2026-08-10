package app;

import database.security.BcryptPasswordHasher;
import interface_adapter.account.change_password.ChangePasswordController;
import interface_adapter.account.change_password.ChangePasswordPresenter;
import interface_adapter.account.change_username.ChangeUsernameController;
import interface_adapter.account.change_username.ChangeUsernamePresenter;
import interface_adapter.account.delete_account.DeleteAccountController;
import interface_adapter.account.delete_account.DeleteAccountPresenter;
import interface_adapter.account.load_account.LoadAccountController;
import interface_adapter.account.load_account.LoadAccountPresenter;
import use_case.account.change_password.ChangePasswordInteractor;
import use_case.account.change_username.ChangeUsernameInteractor;
import use_case.account.delete_account.DeleteAccountInteractor;
import use_case.account.load_account.LoadAccountInteractor;

/** Account-management controller construction. */
final class AccountControllers {
    private final ChangeUsernameController changeUsername;
    private final ChangePasswordController changePassword;
    private final DeleteAccountController deleteAccount;
    private final LoadAccountController loadAccount;

    private AccountControllers(final ChangeUsernameController changeUsername,
                               final ChangePasswordController changePassword,
                               final DeleteAccountController deleteAccount,
                               final LoadAccountController loadAccount) {
        this.changeUsername = changeUsername;
        this.changePassword = changePassword;
        this.deleteAccount = deleteAccount;
        this.loadAccount = loadAccount;
    }

    static AccountControllers create(final ApplicationInfrastructure infrastructure, final AuthModels auth,
                                     final FeatureModels features) {
        final BcryptPasswordHasher passwordHasher = new BcryptPasswordHasher();
        return new AccountControllers(
            new ChangeUsernameController(new ChangeUsernameInteractor(infrastructure.users(), infrastructure.users(),
                new ChangeUsernamePresenter(features.account(), auth.isLoggedIn()))),
            new ChangePasswordController(new ChangePasswordInteractor(infrastructure.users(), infrastructure.users(),
                passwordHasher, new ChangePasswordPresenter(features.account()))),
            new DeleteAccountController(new DeleteAccountInteractor(infrastructure.users(), infrastructure.users(),
                new DeleteAccountPresenter(features.account(), auth.isLoggedIn()))),
            new LoadAccountController(new LoadAccountInteractor(infrastructure.users(),
                new LoadAccountPresenter(features.account()))));
    }

    ChangeUsernameController changeUsername() {
        return changeUsername;
    }

    ChangePasswordController changePassword() {
        return changePassword;
    }

    DeleteAccountController deleteAccount() {
        return deleteAccount;
    }

    LoadAccountController loadAccount() {
        return loadAccount;
    }

}
