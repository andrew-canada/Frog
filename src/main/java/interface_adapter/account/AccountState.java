package interface_adapter.account;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AccountState {

    private final PropertyChangeSupport changes=new PropertyChangeSupport(this);

    private String username;
    private String personalPlan;

    private boolean changeUsernameSuccess;
    private String changeUsernameMessage;

    private boolean changePasswordSuccess;
    private String changePasswordMessage;

    private boolean deleteAccountSuccess;

    private boolean personalPlanSuccess;
    private String personalPlanMessage;

    public AccountState(String username, String personalPlan, boolean changeUsernameSuccess, String changeUsernameMessage, boolean changePasswordSuccess, String changePasswordMessage, boolean deleteAccountSuccess, boolean personalPlanSuccess, String personalPlanMessage) {

        this.username = username;
        this.personalPlan = personalPlan;
        this.changeUsernameSuccess = changeUsernameSuccess;
        this.changeUsernameMessage = changeUsernameMessage;
        this.changePasswordSuccess = changePasswordSuccess;
        this.changePasswordMessage = changePasswordMessage;
        this.deleteAccountSuccess = deleteAccountSuccess;
        this.personalPlanSuccess = personalPlanSuccess;
        this.personalPlanMessage = personalPlanMessage;

    }

    public void addPropertyChangeListener(PropertyChangeListener l) { changes.addPropertyChangeListener(l); }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        String prev = this.username;
        this.username = username;
        changes.firePropertyChange("state", prev, username);
    }

    public String getPersonalPlan() {
        return personalPlan;
    }

    public void setPersonalPlan(String personalPlan) {
        String prev =  this.personalPlan;
        this.personalPlan = personalPlan;
        changes.firePropertyChange("state", prev, personalPlan);
    }

    public boolean getChangeUsernameSuccess() {
        return changeUsernameSuccess;
    }

    public void setChangeUsernameSuccess(boolean changeUsernameSuccess) {
        boolean prev = this.changeUsernameSuccess;
        this.changeUsernameSuccess = changeUsernameSuccess;
        changes.firePropertyChange("state", prev, changeUsernameSuccess);
    }

    public String getChangeUsernameMessage() {
        return changeUsernameMessage;
    }

    public void setChangeUsernameMessage(String changeUsernameMessage) {
        String prev = this.changeUsernameMessage;
        this.changeUsernameMessage = changeUsernameMessage;
        changes.firePropertyChange("state", prev, changeUsernameMessage);

    }

    public boolean getChangePasswordSuccess() {
        return changePasswordSuccess;
    }

    public void setChangePasswordSuccess(boolean changePasswordSuccess) {
        boolean prev = this.changePasswordSuccess;
        this.changePasswordSuccess = changePasswordSuccess;
        changes.firePropertyChange("state", prev, changePasswordSuccess);
    }

    public String getChangePasswordMessage() {
        return changePasswordMessage;
    }

    public void setChangePasswordMessage(String changePasswordMessage) {
        String prev = this.changePasswordMessage;
        this.changePasswordMessage = changePasswordMessage;
        changes.firePropertyChange("state", prev, changePasswordMessage);
    }

    public boolean getDeleteAccountSuccess() {
        return deleteAccountSuccess;
    }

    public void setDeleteAccountSuccess(boolean deleteAccountSuccess) {
        boolean prev = this.deleteAccountSuccess;
        this.deleteAccountSuccess = deleteAccountSuccess;
        changes.firePropertyChange("state", prev, deleteAccountSuccess);
    }

    public boolean getPersonalPlanSuccess() {
        return personalPlanSuccess;
    }

    public void setPersonalPlanSuccess(boolean personalPlanSuccess) {
        boolean prev = this.personalPlanSuccess;
        this.personalPlanSuccess = personalPlanSuccess;
        changes.firePropertyChange("state", prev, personalPlanSuccess);
    }

    public String getPersonalPlanMessage() {
        return personalPlanMessage;
    }

    public void setPersonalPlanMessage(String personalPlanMessage) {
        String prev = this.personalPlanMessage;
        this.personalPlanMessage = personalPlanMessage;
        changes.firePropertyChange("state", prev, personalPlanMessage);
    }

}