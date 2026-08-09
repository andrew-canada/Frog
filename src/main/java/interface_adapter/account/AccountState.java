package interface_adapter.account;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AccountState {

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

    public AccountState(final String username, final String personalPlan, final boolean changeUsernameSuccess,
                        final String changeUsernameMessage, final boolean changePasswordSuccess,
                        final String changePasswordMessage, final boolean deleteAccountSuccess,
                        final String deleteAccountMessage, final boolean personalPlanSuccess,
                        final String personalPlanMessage) {

        this.username = username;
        this.personalPlan = personalPlan;
        this.changeUsernameSuccess = changeUsernameSuccess;
        this.changeUsernameMessage = changeUsernameMessage;
        this.changePasswordSuccess = changePasswordSuccess;
        this.changePasswordMessage = changePasswordMessage;
        this.deleteAccountSuccess = deleteAccountSuccess;
        this.deleteAccountMessage = deleteAccountMessage;
        this.personalPlanSuccess = personalPlanSuccess;
        this.personalPlanMessage = personalPlanMessage;

    }

    public void addPropertyChangeListener(final PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
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

    public void exitResetState() {
        baseResetState();
        changes.firePropertyChange("state", "a", "b");
    }

    public void logoutResetState() {
        baseResetState();
        username = "";
        personalPlan = "";
        changes.firePropertyChange("state", "a", "b");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        final String prev = this.username;
        this.username = username;
        changes.firePropertyChange("username", prev, username);
    }

    public String getPersonalPlan() {
        return personalPlan;
    }

    public void setPersonalPlan(final String personalPlan) {
        final String prev = this.personalPlan;
        this.personalPlan = personalPlan;
        changes.firePropertyChange("personalPlan", prev, personalPlan);
    }

    public boolean getChangeUsernameSuccess() {
        return changeUsernameSuccess;
    }

    public void setChangeUsernameSuccess(final boolean changeUsernameSuccess) {
        final boolean prev = this.changeUsernameSuccess;
        this.changeUsernameSuccess = changeUsernameSuccess;
        changes.firePropertyChange("changeUsernameSuccess", prev, changeUsernameSuccess);
    }

    public String getChangeUsernameMessage() {
        return changeUsernameMessage;
    }

    public void setChangeUsernameMessage(final String changeUsernameMessage) {
        final String prev = this.changeUsernameMessage;
        this.changeUsernameMessage = changeUsernameMessage;
        changes.firePropertyChange("changeUsernameMessage", prev, changeUsernameMessage);

    }

    public boolean getChangePasswordSuccess() {
        return changePasswordSuccess;
    }

    public void setChangePasswordSuccess(final boolean changePasswordSuccess) {
        final boolean prev = this.changePasswordSuccess;
        this.changePasswordSuccess = changePasswordSuccess;
        changes.firePropertyChange("changePasswordSuccess", prev, changePasswordSuccess);
    }

    public String getChangePasswordMessage() {
        return changePasswordMessage;
    }

    public void setChangePasswordMessage(final String changePasswordMessage) {
        final String prev = this.changePasswordMessage;
        this.changePasswordMessage = changePasswordMessage;
        changes.firePropertyChange("changePasswordMessage", prev, changePasswordMessage);
    }

    public boolean getDeleteAccountSuccess() {
        return deleteAccountSuccess;
    }

    public void setDeleteAccountSuccess(final boolean deleteAccountSuccess) {
        final boolean prev = this.deleteAccountSuccess;
        this.deleteAccountSuccess = deleteAccountSuccess;
        changes.firePropertyChange("deleteAccountSuccess", prev, deleteAccountSuccess);
    }

    public String getDeleteAccountMessage() {
        return deleteAccountMessage;
    }

    public void setDeleteAccountMessage(final String deleteAccountMessage) {
        final String prev = this.deleteAccountMessage;
        this.deleteAccountMessage = deleteAccountMessage;
        changes.firePropertyChange("deleteAccountMessage", prev, deleteAccountMessage);
    }

    public boolean getPersonalPlanSuccess() {
        return personalPlanSuccess;
    }

    public void setPersonalPlanSuccess(final boolean personalPlanSuccess) {
        final boolean prev = this.personalPlanSuccess;
        this.personalPlanSuccess = personalPlanSuccess;
        changes.firePropertyChange("personalPlanSuccess", prev, personalPlanSuccess);
    }

    public String getPersonalPlanMessage() {
        return personalPlanMessage;
    }

    public void setPersonalPlanMessage(final String personalPlanMessage) {
        final String prev = this.personalPlanMessage;
        this.personalPlanMessage = personalPlanMessage;
        changes.firePropertyChange("personalPlanMessage", prev, personalPlanMessage);
    }

}
