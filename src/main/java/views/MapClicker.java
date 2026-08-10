package views;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.function.BiConsumer;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * Action Listener for the "Right Click on map to select location" button in the LocationInputDialog.
 *
 * <p>Adds a MouseAdapter to the given map which listens for right clicks, converts the coordinates from the
 * mouse coordinates to latitude and longitude, and runs onSave on the latitude and longitude before removing
 * itself as a MouseAdapter.</p>
 */

class MapClicker implements ActionListener {
    private final MainView.CampusMapPanel map;
    private BiConsumer<Double, Double> onSave;
    private Window frame;

    MapClicker(final MainView.CampusMapPanel map) {
        this.map = map;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        class MapClickListener extends MouseAdapter {
            private final MainView.CampusMapPanel map;
            private final BiConsumer<Double, Double> onSave;

            MapClickListener(final MainView.CampusMapPanel map, final BiConsumer<Double, Double> onSave) {
                this.map = map;
                this.onSave = onSave;
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    final Point pt = e.getPoint();
                    final GeoPosition pos = map.convertPointToGeoPosition(pt);
                    onSave.accept(pos.getLatitude(), pos.getLongitude());
                    map.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    map.removeMouseListener(this);
                }
            }
        }

        map.addMouseListener(new MapClickListener(map, onSave));
        map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public void addOnSave(final BiConsumer<Double, Double> saveHandler) {
        this.onSave = saveHandler;
    }

    public void addFrame(final Window window) {
        this.frame = window;
    }
}
