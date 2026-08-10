package interface_adapter.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractViewModel<S> {
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private S state;

    protected AbstractViewModel(final S initial) {
        state = initial;
    }

    public S getState() {
        return state;
    }

    /**
     * Performs this operation.
     *
     * @param next parameter value.
     */
    public void setState(final S next) {
        state = next;
        // Fire with a null old value so PropertyChangeSupport never suppresses the event when the
        // new state equals the previous one.
        changes.firePropertyChange("state", null, next);
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
     * Performs this operation.
     *
     * @param labelValue parameter value.
     */
    public void removePropertyChangeListener(final PropertyChangeListener labelValue) {
        changes.removePropertyChangeListener(labelValue);
    }
}
