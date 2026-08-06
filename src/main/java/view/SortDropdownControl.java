package view;

import javax.swing.*;

public final class SortDropdownControl extends JComboBox<String> {
    public SortDropdownControl() {
        super(new String[]{"Alphabetical"});
        setBackground(Theme.PAPER);
    }
}
