package view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

final class Theme {
    static final Color CREAM=new Color(244,242,236), PAPER=Color.WHITE, INK=new Color(29,29,27),
            MUTED=new Color(108,107,99), LINE=new Color(228,226,218), BLUE=new Color(24,95,165),
            PALE_BLUE=new Color(230,241,251), GOLD=new Color(239,159,39), BERRY=new Color(153,53,86);
    private Theme(){}
    static Border pad(int t,int l,int b,int r){return BorderFactory.createEmptyBorder(t,l,b,r);}
    static JLabel label(String text,int size,Color color){JLabel l=new JLabel(text);l.setFont(l.getFont().deriveFont((float)size));l.setForeground(color);return l;}
    static JLabel title(String text){JLabel l=label(text,22,INK);l.setFont(l.getFont().deriveFont(Font.BOLD));return l;}
    static JButton button(String text){JButton b=new JButton(text);b.setFocusPainted(false);b.setOpaque(true);b.setContentAreaFilled(true);b.setBackground(PAPER);b.setForeground(Color.BLACK);b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),pad(8,14,8,14)));return b;}
    static JButton primary(String text){return button(text);}
    static JPanel page(){JPanel p=new JPanel();p.setBackground(PAPER);p.setBorder(pad(20,24,20,24));return p;}
}
