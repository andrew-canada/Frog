package views;

import javax.swing.SwingUtilities;

import interface_adapter.common.UiDispatcher;

/**
 * Swing-specific implementation kept at the UI framework boundary.
 */
public final class SwingUiDispatcher implements UiDispatcher {
    @Override
    public void dispatch(final Runnable update) {
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        }
        else {
            SwingUtilities.invokeLater(update);
        }
    }
}
