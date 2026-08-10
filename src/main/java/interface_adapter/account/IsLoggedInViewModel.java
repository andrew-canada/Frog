package interface_adapter.account;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import interface_adapter.common.AbstractViewModel;

public final class IsLoggedInViewModel extends AbstractViewModel<IsLoggedInState> {

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public IsLoggedInViewModel() {
        super(new IsLoggedInState(false, ""));
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
        changes.firePropertyChange("state", prev, username);
    }

    public boolean getIsLoggedIn() {
        return getState().getIsLoggedIn();
    }

    /**
     * Performs this operation.
     *
     * @param isLoggedIn parameter value.
     */
    public void setIsLoggedIn(final boolean isLoggedIn) {
        final boolean prev = getState().getIsLoggedIn();
        getState().setIsLoggedIn(isLoggedIn);
        changes.firePropertyChange("state", prev, isLoggedIn);
    }

}
