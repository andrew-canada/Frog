package view;

import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.BiConsumer;

public class MapClicker implements ActionListener {
    MainView.CampusMapPanel map;
    BiConsumer<Double, Double> onSave;
    Window frame;

    public MapClicker(MainView.CampusMapPanel map) {
        this.map = map;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("button clicked!");
        class MapClickListener extends MouseAdapter {
            MainView.CampusMapPanel map;
            BiConsumer<Double, Double> onSave;

            public MapClickListener(MainView.CampusMapPanel map, BiConsumer<Double, Double> onSave) {
                this.map = map;
                this.onSave = onSave;
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    Point pt = e.getPoint();
                    GeoPosition pos = map.convertPointToGeoPosition(pt);
                    onSave.accept(pos.getLatitude(), pos.getLongitude());
                    map.removeMouseListener(this);
                }
            }
        };

        map.addMouseListener(new MapClickListener(map, onSave));
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public void addOnSave(BiConsumer<Double, Double> onSave) {
        this.onSave = onSave;
    }

    public void addFrame(Window frame) {
        this.frame = frame;
    }
};
