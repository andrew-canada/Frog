package interface_adapter.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class ViewModel<S> {
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private S state;

    protected ViewModel(final S initial) {
        state = initial;
    }

    public S getState() {
        return state;
    }

    public void setState(final S next) {
        state = next;
        // Fire with a null old value so PropertyChangeSupport never suppresses the event when the
        // new state equals the previous one.
        changes.firePropertyChange("state", null, next);
    }

    public void addPropertyChangeListener(final PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(final PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }
}
