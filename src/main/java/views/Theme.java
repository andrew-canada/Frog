package views;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

final class Theme {
    static final Color CREAM = new Color(244, 242, 236), PAPER = Color.WHITE, INK = new Color(29, 29, 27), MUTED =
        new Color(108, 107, 99), LINE = new Color(228, 226, 218), BLUE = new Color(24, 95, 165), PALE_BLUE =
        new Color(230, 241, 251), GOLD = new Color(239, 159, 39), BERRY = new Color(153, 53, 86), COLORBLIND_BLUE =
        new Color(0, 114, 178), COLORBLIND_ORANGE = new Color(230, 159, 0), NO_DATA = new Color(216, 210, 196),
        BRIGHT_GREEN = new Color(22, 181, 78), PALE_GREEN = new Color(223, 247, 231);

    private Theme() {
    }

    static Border pad(final int t, final int l, final int b, final int r) {
        return BorderFactory.createEmptyBorder(t, l, b, r);
    }

    static JLabel label(final String text, final int size, final Color color) {
        final JLabel l = new JLabel(text);
        l.setFont(l
            .getFont()
            .deriveFont((float) size));
        l.setForeground(color);
        return l;
    }

    static JLabel title(final String text) {
        final JLabel l = label(text, 22, INK);
        l.setFont(l
            .getFont()
            .deriveFont(Font.BOLD));
        return l;
    }

    static JButton button(final String text) {
        final JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBackground(PAPER);
        b.setForeground(Color.BLACK);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), pad(8, 14, 8, 14)));
        return b;
    }

    static JButton primary(final String text) {
        return button(text);
    }

    static JPanel page() {
        final JPanel p = new JPanel();
        p.setBackground(PAPER);
        p.setBorder(pad(20, 24, 20, 24));
        return p;
    }
}
