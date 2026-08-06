package view;

import entity.Washroom;
import interface_adapter.filter.FilterController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class FilterView extends JFrame {
    FilterController controller;
    FilterPanel filterPanel;

    public FilterView (Window owner, String title, String selected, FilterController controller, double latitude, double longitude ) {
        this.controller = controller;

        FilterPanel p = new FilterPanel(this, selected, () ->
        {controller.execute(
                        filterPanel.busyness(),
                        filterPanel.cleanliness(),
                        filterPanel.accessibleOnly(),
                        filterPanel.ownReviews(),
                        filterPanel.selectedBuilding(),
                        selected,
                        filterPanel.gender(),
                        latitude,
                        longitude
                );
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));});
        this.filterPanel = p;
        this.setContentPane(p);
        this.pack();
        this.setLocationRelativeTo(owner);

    }

    public int busyness(){
        return filterPanel.busyness();
    }

    public int cleanliness(){
        return filterPanel.cleanliness();
    }

    public boolean accessibleOnly() {
        return filterPanel.accessibleOnly();
    }

    public boolean ownReviews() {
        return filterPanel.ownReviews();
    }

    public boolean selectedBuilding() {
        return filterPanel.selectedBuilding();
    }

    public Washroom.Gender gender() {
        return filterPanel.gender();
    }


}
