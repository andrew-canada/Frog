package view;

import javax.swing.*;

public final class SortDropdownControl extends JComboBox<String> {
    public SortDropdownControl() {
        super(new String[]{"Nearest", "Highest rated"});
        setBackground(Theme.PAPER);
    }
}
