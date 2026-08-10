package interface_adapter.account;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AccountState {
    private static final int VALUE_COUNT = 10;
    private static final int PERSONAL_PLAN_INDEX = 1;
    private static final int CHANGE_USERNAME_SUCCESS_INDEX = 2;
    private static final int CHANGE_USERNAME_MESSAGE_INDEX = 3;
    private static final int CHANGE_PASSWORD_SUCCESS_INDEX = 4;
    private static final int CHANGE_PASSWORD_MESSAGE_INDEX = 5;
    private static final int DELETE_ACCOUNT_SUCCESS_INDEX = 6;
    private static final int DELETE_ACCOUNT_MESSAGE_INDEX = 7;
    private static final int PERSONAL_PLAN_SUCCESS_INDEX = 8;
    private static final int PERSONAL_PLAN_MESSAGE_INDEX = 9;

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    private String username;
    private String personalPlan;

    private boolean changeUsernameSuccess;
    private String changeUsernameMessage;

    private boolean changePasswordSuccess;
    private String changePasswordMessage;

    private boolean deleteAccountSuccess;
    private String deleteAccountMessage;

    private boolean personalPlanSuccess;
    private String personalPlanMessage;

    public AccountState(final Object... values) {
        if (values.length != VALUE_COUNT) {
            throw new IllegalArgumentException("AccountState requires ten values");
        }
        this.username = (String) values[0];
        this.personalPlan = (String) values[PERSONAL_PLAN_INDEX];
        this.changeUsernameSuccess = (Boolean) values[CHANGE_USERNAME_SUCCESS_INDEX];
        this.changeUsernameMessage = (String) values[CHANGE_USERNAME_MESSAGE_INDEX];
        this.changePasswordSuccess = (Boolean) values[CHANGE_PASSWORD_SUCCESS_INDEX];
        this.changePasswordMessage = (String) values[CHANGE_PASSWORD_MESSAGE_INDEX];
        this.deleteAccountSuccess = (Boolean) values[DELETE_ACCOUNT_SUCCESS_INDEX];
        this.deleteAccountMessage = (String) values[DELETE_ACCOUNT_MESSAGE_INDEX];
        this.personalPlanSuccess = (Boolean) values[PERSONAL_PLAN_SUCCESS_INDEX];
        this.personalPlanMessage = (String) values[PERSONAL_PLAN_MESSAGE_INDEX];

    }

    /**
     * Performs this operation.
     *
     * @param labelValue parameter value.
     */
    public void addPropertyChangeListener(final PropertyChangeListener labelValue) {
        changes.addPropertyChangeListener(labelValue);
    }

    private void baseResetState() {
        changeUsernameSuccess = false;
        changeUsernameMessage = "";
        changePasswordSuccess = false;
        changePasswordMessage = "";
        deleteAccountSuccess = false;
        deleteAccountMessage = "";
        personalPlanSuccess = false;
        personalPlanMessage = "";
    }

    /**
     * Performs this operation.
     */
    public void exitResetState() {
        baseResetState();
        changes.firePropertyChange("state", "a", "b");
    }

    /**
     * Performs this operation.
     */
    public void logoutResetState() {
        baseResetState();
        username = "";
        personalPlan = "";
        changes.firePropertyChange("state", "a", "b");
    }

    public String getUsername() {
        return username;
    }

    /**
     * Performs this operation.
     *
     * @param username parameter value.
     */
    public void setUsername(final String username) {
        final String prev = this.username;
        this.username = username;
        changes.firePropertyChange("username", prev, username);
    }

    public String getPersonalPlan() {
        return personalPlan;
    }

    /**
     * Performs this operation.
     *
     * @param personalPlan parameter value.
     */
    public void setPersonalPlan(final String personalPlan) {
        final String prev = this.personalPlan;
        this.personalPlan = personalPlan;
        changes.firePropertyChange("personalPlan", prev, personalPlan);
    }

    public boolean getChangeUsernameSuccess() {
        return changeUsernameSuccess;
    }

    /**
     * Performs this operation.
     *
     * @param changeUsernameSuccess parameter value.
     */
    public void setChangeUsernameSuccess(final boolean changeUsernameSuccess) {
        final boolean prev = this.changeUsernameSuccess;
        this.changeUsernameSuccess = changeUsernameSuccess;
        changes.firePropertyChange("changeUsernameSuccess", prev, changeUsernameSuccess);
    }

    public String getChangeUsernameMessage() {
        return changeUsernameMessage;
    }

    /**
     * Performs this operation.
     *
     * @param changeUsernameMessage parameter value.
     */
    public void setChangeUsernameMessage(final String changeUsernameMessage) {
        final String prev = this.changeUsernameMessage;
        this.changeUsernameMessage = changeUsernameMessage;
        changes.firePropertyChange("changeUsernameMessage", prev, changeUsernameMessage);

    }

    public boolean getChangePasswordSuccess() {
        return changePasswordSuccess;
    }

    /**
     * Performs this operation.
     *
     * @param changePasswordSuccess parameter value.
     */
    public void setChangePasswordSuccess(final boolean changePasswordSuccess) {
        final boolean prev = this.changePasswordSuccess;
        this.changePasswordSuccess = changePasswordSuccess;
        changes.firePropertyChange("changePasswordSuccess", prev, changePasswordSuccess);
    }

    public String getChangePasswordMessage() {
        return changePasswordMessage;
    }

    /**
     * Performs this operation.
     *
     * @param changePasswordMessage parameter value.
     */
    public void setChangePasswordMessage(final String changePasswordMessage) {
        final String prev = this.changePasswordMessage;
        this.changePasswordMessage = changePasswordMessage;
        changes.firePropertyChange("changePasswordMessage", prev, changePasswordMessage);
    }

    public boolean getDeleteAccountSuccess() {
        return deleteAccountSuccess;
    }

    /**
     * Performs this operation.
     *
     * @param deleteAccountSuccess parameter value.
     */
    public void setDeleteAccountSuccess(final boolean deleteAccountSuccess) {
        final boolean prev = this.deleteAccountSuccess;
        this.deleteAccountSuccess = deleteAccountSuccess;
        changes.firePropertyChange("deleteAccountSuccess", prev, deleteAccountSuccess);
    }

    public String getDeleteAccountMessage() {
        return deleteAccountMessage;
    }

    /**
     * Performs this operation.
     *
     * @param deleteAccountMessage parameter value.
     */
    public void setDeleteAccountMessage(final String deleteAccountMessage) {
        final String prev = this.deleteAccountMessage;
        this.deleteAccountMessage = deleteAccountMessage;
        changes.firePropertyChange("deleteAccountMessage", prev, deleteAccountMessage);
    }

    public boolean getPersonalPlanSuccess() {
        return personalPlanSuccess;
    }

    /**
     * Performs this operation.
     *
     * @param personalPlanSuccess parameter value.
     */
    public void setPersonalPlanSuccess(final boolean personalPlanSuccess) {
        final boolean prev = this.personalPlanSuccess;
        this.personalPlanSuccess = personalPlanSuccess;
        changes.firePropertyChange("personalPlanSuccess", prev, personalPlanSuccess);
    }

    public String getPersonalPlanMessage() {
        return personalPlanMessage;
    }

    /**
     * Performs this operation.
     *
     * @param personalPlanMessage parameter value.
     */
    public void setPersonalPlanMessage(final String personalPlanMessage) {
        final String prev = this.personalPlanMessage;
        this.personalPlanMessage = personalPlanMessage;
        changes.firePropertyChange("personalPlanMessage", prev, personalPlanMessage);
    }

}
