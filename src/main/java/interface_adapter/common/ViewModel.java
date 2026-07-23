package interface_adapter.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class ViewModel<S> {
    private final PropertyChangeSupport changes=new PropertyChangeSupport(this);
    private S state;
    protected ViewModel(S initial) { state=initial; }
    public S getState() { return state; }
    public void setState(S next) { S old=state; state=next; changes.firePropertyChange("state",old,next); }
    public void addPropertyChangeListener(PropertyChangeListener l) { changes.addPropertyChangeListener(l); }
    public void removePropertyChangeListener(PropertyChangeListener l) { changes.removePropertyChangeListener(l); }
}
