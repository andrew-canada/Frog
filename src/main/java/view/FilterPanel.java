package view;

import entity.Washroom;
import javax.swing.*;
import java.awt.*;

public final class FilterPanel extends JPanel{
    private final JCheckBox accessible=new JCheckBox("Accessible only");
    private final JComboBox<String> gender=new JComboBox<>(new String[]{"Any gender","All-gender","Women","Men"});
    public FilterPanel(){setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));setBackground(Theme.PAPER);setBorder(Theme.pad(12,12,12,12));
        JLabel heading=Theme.label("Filters",16,Theme.INK);heading.setFont(heading.getFont().deriveFont(Font.BOLD));add(heading);add(Box.createVerticalStrut(10));
        accessible.setBackground(Theme.PAPER);add(accessible);add(Box.createVerticalStrut(8));add(Theme.label("Washroom type",12,Theme.MUTED));add(gender);}
    public boolean accessibleOnly(){return accessible.isSelected();}
    public Washroom.Gender gender(){return switch(gender.getSelectedIndex()){case 1->Washroom.Gender.ALL_GENDER;case 2->Washroom.Gender.WOMEN;case 3->Washroom.Gender.MEN;default->null;};}
}
