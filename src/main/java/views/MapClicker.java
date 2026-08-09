package views;

import org.jxmapviewer.viewer.GeoPosition;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.function.BiConsumer;

public class MapClicker implements ActionListener {
    MainView.CampusMapPanel map;
    BiConsumer<Double, Double> onSave;
    Window frame;

    public MapClicker(final MainView.CampusMapPanel map) {
        this.map = map;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        System.out.println("button clicked!");
        class MapClickListener extends MouseAdapter {
            final MainView.CampusMapPanel map;
            final BiConsumer<Double, Double> onSave;

            public MapClickListener(final MainView.CampusMapPanel map, final BiConsumer<Double, Double> onSave) {
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

    public void addOnSave(final BiConsumer<Double, Double> onSave) {
        this.onSave = onSave;
    }

    public void addFrame(final Window frame) {
        this.frame = frame;
    }
}
