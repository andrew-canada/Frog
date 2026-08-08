package interface_adapter.account;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class IsLoggedInState {

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    private boolean isLoggedIn;
    private String username;

    public IsLoggedInState(boolean isLoggedIn, String username) {

        this.isLoggedIn = isLoggedIn;
        this.username = username;

    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        String prev = this.username;
        this.username = username;
        changes.firePropertyChange("state", prev, username);
    }

    public boolean getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        boolean prev = this.isLoggedIn;
        this.isLoggedIn = isLoggedIn;
        changes.firePropertyChange("state", prev, isLoggedIn);
    }

}
