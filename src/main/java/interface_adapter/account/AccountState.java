package interface_adapter.account;

public class AccountState {

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPersonalPlan() {
        return personalPlan;
    }

    public void setPersonalPlan(String personalPlan) {
        this.personalPlan = personalPlan;
    }

    public boolean getChangeUsernameSuccess() {
        return changeUsernameSuccess;
    }

    public void setChangeUsernameSuccess(boolean changeUsernameSuccess) {
        this.changeUsernameSuccess = changeUsernameSuccess;
    }

    public String getChangeUsernameMessage() {
        return changeUsernameMessage;
    }

    public void setChangeUsernameMessage(String changeUsernameMessage) {
        this.changeUsernameMessage = changeUsernameMessage;
    }

    public boolean getChangePasswordSuccess() {
        return changePasswordSuccess;
    }

    public void setChangePasswordSuccess(boolean changePasswordSuccess) {
        this.changePasswordSuccess = changePasswordSuccess;
    }

    public String getChangePasswordMessage() {
        return changePasswordMessage;
    }

    public void setChangePasswordMessage(String changePasswordMessage) {
        this.changePasswordMessage = changePasswordMessage;
    }

    public boolean getDeleteAccountSuccess() {
        return deleteAccountSuccess;
    }

    public void setDeleteAccountSuccess(boolean deleteAccountSuccess) {
        this.deleteAccountSuccess = deleteAccountSuccess;
    }

    public boolean getPersonalPlanSuccess() {
        return personalPlanSuccess;
    }

    public void setPersonalPlanSuccess(boolean personalPlanSuccess) {
        this.personalPlanSuccess = personalPlanSuccess;
    }

    public String getPersonalPlanMessage() {
        return personalPlanMessage;
    }

    public void setPersonalPlanMessage(String personalPlanMessage) {
        this.personalPlanMessage = personalPlanMessage;
    }

}