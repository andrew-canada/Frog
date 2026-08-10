package views;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

final class CardClickListener extends MouseAdapter {
    private final Runnable action;

    CardClickListener(final Runnable action) {
        this.action = action;
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        action.run();
    }
}
