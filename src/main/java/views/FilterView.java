package views;

import java.awt.Window;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import entity.Washroom;
import interface_adapter.filter.FilterController;

class FilterView extends JFrame {
    private FilterController controller;
    private FilterPanel filterPanel;

    public FilterView(final Window owner, final String title, final String selected, final FilterController controller,
                      final double latitude, final double longitude) {
        this.controller = controller;

        final FilterPanel p = new FilterPanel(this, selected, () -> {
            controller.execute(filterPanel.busyness(), filterPanel.cleanliness(), filterPanel.accessibleOnly(),
                filterPanel.ownReviews(), filterPanel.selectedBuilding(), filterPanel.personalPlan(), selected,
                filterPanel.gender(), latitude, longitude);
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        this.filterPanel = p;
        this.setContentPane(p);
        this.pack();
        this.setLocationRelativeTo(owner);

    }

    public int busyness() {
        return filterPanel.busyness();
    }

    public int cleanliness() {
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
