package views;

import javax.swing.JComboBox;

final class ReviewSortDropdownControl extends JComboBox<String> {
    ReviewSortDropdownControl() {
        super(new String[] {"Highest Rated", "Lowest Rated", "Most Helpful", "Newest", "Voted by Me"});
        setBackground(Theme.PAPER);
    }
}
