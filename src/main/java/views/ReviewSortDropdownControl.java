package views;

import javax.swing.JComboBox;

public final class ReviewSortDropdownControl extends JComboBox<String> {
    public ReviewSortDropdownControl() {
        super(new String[] {"Highest Rated", "Lowest Rated", "Most Helpful", "Newest", "Voted by Me"});
        setBackground(Theme.PAPER);
    }
}
