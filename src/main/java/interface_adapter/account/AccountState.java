package interface_adapter.account;

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
    }

    /**
     * Performs this operation.
     */
    public void logoutResetState() {
        baseResetState();
        username = "";
        personalPlan = "";
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
        this.username = username;
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
        this.personalPlan = personalPlan;
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
        this.changeUsernameSuccess = changeUsernameSuccess;
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
        this.changeUsernameMessage = changeUsernameMessage;
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
        this.changePasswordSuccess = changePasswordSuccess;
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
        this.changePasswordMessage = changePasswordMessage;
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
        this.deleteAccountSuccess = deleteAccountSuccess;
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
        this.deleteAccountMessage = deleteAccountMessage;
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
        this.personalPlanSuccess = personalPlanSuccess;
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
        this.personalPlanMessage = personalPlanMessage;
    }

}
