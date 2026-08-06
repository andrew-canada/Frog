package view;

import javax.swing.*;

public final class WashroomSortDropdownControl extends JComboBox<String> {
    public WashroomSortDropdownControl() {
        super(new String[]{"Alphabetical", "Nearest", "Highest Rated"});
        setBackground(Theme.PAPER);
    }
}
