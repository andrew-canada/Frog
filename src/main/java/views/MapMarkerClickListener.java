package views;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

final class MapMarkerClickListener extends MouseAdapter {
    private final Map<String, Rectangle> targets;
    private final Consumer<String> selection;

    MapMarkerClickListener(final Map<String, Rectangle> targets, final Consumer<String> selection) {
        this.targets = targets;
        this.selection = selection;
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        if (SwingUtilities.isLeftMouseButton(event)) {
            for (final Map.Entry<String, Rectangle> target : targets.entrySet()) {
                if (target.getValue().contains(event.getPoint())) {
                    selection.accept(target.getKey());
                }
            }
        }
    }
}
