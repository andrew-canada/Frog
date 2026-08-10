package interface_adapter.common;

/**
 * Schedules a view-model update on the UI framework's event thread.
 */
@FunctionalInterface
public interface UiDispatcher {
    /**
     * Performs this operation.
     *
     * @param update parameter value.
     */
    void dispatch(Runnable update);
}
