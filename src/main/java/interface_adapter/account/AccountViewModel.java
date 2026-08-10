package interface_adapter.account;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import interface_adapter.common.AbstractViewModel;

public final class AccountViewModel extends AbstractViewModel<AccountState> {

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public AccountViewModel() {
        super(new AccountState("", null, false, "", false, "", false, "", false, ""));
    }

    /**
     * Performs this operation.
     *
     * @param labelValue parameter value.
     */
    public void addPropertyChangeListener(final PropertyChangeListener labelValue) {
        changes.addPropertyChangeListener(labelValue);
    }

    /**
     * Resents the state when user exits the view.
     */
    public void exitResetState() {
        getState().exitResetState();
        changes.firePropertyChange("state", "a", "b");
    }

    /**
     * Resets the state when user logs out or deletes account.
     */
    public void logoutResetState() {
        getState().logoutResetState();
        changes.firePropertyChange("state", "a", "b");
    }

    public String getUsername() {
        return getState().getUsername();
    }

    /**
     * Performs this operation.
     *
     * @param username parameter value.
     */
    public void setUsername(final String username) {
        final String prev = getState().getUsername();
        getState().setUsername(username);
        changes.firePropertyChange("username", prev, username);
    }

    public String getPersonalPlan() {
        return getState().getPersonalPlan();
    }

    /**
     * Performs this operation.
     *
     * @param personalPlan parameter value.
     */
    public void setPersonalPlan(final String personalPlan) {
        final String prev = getState().getPersonalPlan();
        getState().setPersonalPlan(personalPlan);
        changes.firePropertyChange("personalPlan", prev, personalPlan);
    }

    public boolean getChangeUsernameSuccess() {
        return getState().getChangeUsernameSuccess();
    }

    /**
     * Performs this operation.
     *
     * @param changeUsernameSuccess parameter value.
     */
    public void setChangeUsernameSuccess(final boolean changeUsernameSuccess) {
        final boolean prev = getState().getChangeUsernameSuccess();
        getState().setChangeUsernameSuccess(changeUsernameSuccess);
        changes.firePropertyChange("changeUsernameSuccess", prev, changeUsernameSuccess);
    }

    public String getChangeUsernameMessage() {
        return getState().getChangeUsernameMessage();
    }

    /**
     * Performs this operation.
     *
     * @param changeUsernameMessage parameter value.
     */
    public void setChangeUsernameMessage(final String changeUsernameMessage) {
        final String prev = getState().getChangeUsernameMessage();
        getState().setChangeUsernameMessage(changeUsernameMessage);
        changes.firePropertyChange("changeUsernameMessage", prev, changeUsernameMessage);

    }

    public boolean getChangePasswordSuccess() {
        return getState().getChangePasswordSuccess();
    }

    /**
     * Performs this operation.
     *
     * @param changePasswordSuccess parameter value.
     */
    public void setChangePasswordSuccess(final boolean changePasswordSuccess) {
        final boolean prev = getState().getChangePasswordSuccess();
        getState().setChangePasswordSuccess(changePasswordSuccess);
        changes.firePropertyChange("changePasswordSuccess", prev, changePasswordSuccess);
    }

    public String getChangePasswordMessage() {
        return getState().getChangePasswordMessage();
    }

    /**
     * Performs this operation.
     *
     * @param changePasswordMessage parameter value.
     */
    public void setChangePasswordMessage(final String changePasswordMessage) {
        final String prev = getState().getChangePasswordMessage();
        getState().setChangePasswordMessage(changePasswordMessage);
        changes.firePropertyChange("changePasswordMessage", prev, changePasswordMessage);
    }

    public boolean getDeleteAccountSuccess() {
        return getState().getDeleteAccountSuccess();
    }

    /**
     * Performs this operation.
     *
     * @param deleteAccountSuccess parameter value.
     */
    public void setDeleteAccountSuccess(final boolean deleteAccountSuccess) {
        final boolean prev = getState().getDeleteAccountSuccess();
        getState().setDeleteAccountSuccess(deleteAccountSuccess);
        changes.firePropertyChange("deleteAccountSuccess", prev, deleteAccountSuccess);
    }

    public String getDeleteAccountMessage() {
        return getState().getDeleteAccountMessage();
    }

    /**
     * Performs this operation.
     *
     * @param deleteAccountMessage parameter value.
     */
    public void setDeleteAccountMessage(final String deleteAccountMessage) {
        final String prev = getState().getDeleteAccountMessage();
        getState().setDeleteAccountMessage(deleteAccountMessage);
        changes.firePropertyChange("deleteAccountMessage", prev, deleteAccountMessage);
    }

    public boolean getPersonalPlanSuccess() {
        return getState().getPersonalPlanSuccess();
    }

    /**
     * Performs this operation.
     *
     * @param personalPlanSuccess parameter value.
     */
    public void setPersonalPlanSuccess(final boolean personalPlanSuccess) {
        final boolean prev = getState().getPersonalPlanSuccess();
        getState().setPersonalPlanSuccess(personalPlanSuccess);
        changes.firePropertyChange("personalPlanSuccess", prev, personalPlanSuccess);
    }

    public String getPersonalPlanMessage() {
        return getState().getPersonalPlanMessage();
    }

    /**
     * Performs this operation.
     *
     * @param personalPlanMessage parameter value.
     */
    public void setPersonalPlanMessage(final String personalPlanMessage) {
        final String prev = getState().getPersonalPlanMessage();
        getState().setPersonalPlanMessage(personalPlanMessage);
        changes.firePropertyChange("personalPlanMessage", prev, personalPlanMessage);
    }

}
