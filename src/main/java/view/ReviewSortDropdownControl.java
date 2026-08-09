package view;
import javax.swing.*;
import java.awt.*;

public final class ReviewSortDropdownControl extends JComboBox<String> {
    public ReviewSortDropdownControl() {
        super(new String[]{"Relevant", "Most Helpful", "Highest Rated", "Lowest Rated", "Newest", "Voted by Me"});
        setBackground(Theme.PAPER);
        setPreferredSize(new Dimension(200, this.getPreferredSize().height));
    }
}
