package interface_adapter.common;

/** Schedules a view-model update on the UI framework's event thread. */
@FunctionalInterface
public interface UiDispatcher {
    void dispatch(Runnable update);
}
