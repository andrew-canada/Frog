package interface_adapter.account;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class IsLoggedInState {

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    private boolean isLoggedIn;
    private String username;

    public IsLoggedInState(final boolean isLoggedIn, final String username) {

        this.isLoggedIn = isLoggedIn;
        this.username = username;

    }

    /**
     * Performs this operation.
     *
     * @param labelValue parameter value.
     */
    public void addPropertyChangeListener(final PropertyChangeListener labelValue) {
        changes.addPropertyChangeListener(labelValue);
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
        changes.firePropertyChange("state", prev, username);
    }

    public boolean getIsLoggedIn() {
        return isLoggedIn;
    }

    /**
     * Performs this operation.
     *
     * @param isLoggedIn parameter value.
     */
    public void setIsLoggedIn(final boolean isLoggedIn) {
        final boolean prev = this.isLoggedIn;
        this.isLoggedIn = isLoggedIn;
        changes.firePropertyChange("state", prev, isLoggedIn);
    }

}
