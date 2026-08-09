package view;

import interface_adapter.common.UiDispatcher;

import javax.swing.SwingUtilities;

/** Swing-specific implementation kept at the UI framework boundary. */
public final class SwingUiDispatcher implements UiDispatcher {
    @Override
    public void dispatch(Runnable update) {
        if (SwingUtilities.isEventDispatchThread()) update.run();
        else SwingUtilities.invokeLater(update);
    }
}
