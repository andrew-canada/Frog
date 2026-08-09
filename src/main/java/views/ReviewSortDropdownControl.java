package views;

import javax.swing.*;

public final class ReviewSortDropdownControl extends JComboBox<String> {
    public ReviewSortDropdownControl() {
        super(new String[]{"Highest Rated", "Lowest Rated", "Most Helpful", "Newest", "Voted by Me"});
        setBackground(Theme.PAPER);
    }
}
