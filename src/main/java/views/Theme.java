package views;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

final class Theme {
    static final Color CREAM = new Color(244, 242, 236);
    static final Color PAPER = Color.WHITE;
    static final Color INK = new Color(29, 29, 27);
    static final Color MUTED = new Color(108, 107, 99);
    static final Color LINE = new Color(228, 226, 218);
    static final Color BLUE = new Color(24, 95, 165);
    static final Color PALE_BLUE = new Color(230, 241, 251);
    static final Color GOLD = new Color(239, 159, 39);
    static final Color BERRY = new Color(153, 53, 86);
    static final Color COLORBLIND_BLUE = new Color(0, 114, 178);
    static final Color COLORBLIND_ORANGE = new Color(230, 159, 0);
    static final Color NO_DATA = new Color(216, 210, 196);
    static final Color BRIGHT_GREEN = new Color(22, 181, 78);
    static final Color PALE_GREEN = new Color(223, 247, 231);
    private static final int PAGE_HORIZONTAL_PADDING = 24;
    private static final int PAGE_VERTICAL_PADDING = 20;
    private static final int CONTROL_PADDING = 14;
    private static final int SMALL_CONTROL_PADDING = 8;

    private Theme() {
    }

    static Border pad(final int parameterValue, final int labelValue, final int secondValue, final int reviewValue) {
        return BorderFactory.createEmptyBorder(parameterValue, labelValue, secondValue, reviewValue);
    }

    static JLabel label(final String text, final int size, final Color color) {
        final JLabel labelValue = new JLabel(text);
        labelValue.setFont(labelValue
            .getFont()
            .deriveFont((float) size));
        labelValue.setForeground(color);
        return labelValue;
    }

    static JLabel title(final String text) {
        final JLabel labelValue = label(text, 22, INK);
        labelValue.setFont(labelValue
            .getFont()
            .deriveFont(Font.BOLD));
        return labelValue;
    }

    static JButton button(final String text) {
        final JButton secondValue = new JButton(text);
        secondValue.setFocusPainted(false);
        secondValue.setOpaque(true);
        secondValue.setContentAreaFilled(true);
        secondValue.setBackground(PAPER);
        secondValue.setForeground(Color.BLACK);
        secondValue.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
            pad(SMALL_CONTROL_PADDING,
            CONTROL_PADDING, SMALL_CONTROL_PADDING, CONTROL_PADDING)));
        return secondValue;
    }

    static JButton primary(final String text) {
        return button(text);
    }

    static JPanel page() {
        final JPanel p = new JPanel();
        p.setBackground(PAPER);
        p.setBorder(pad(PAGE_VERTICAL_PADDING, PAGE_HORIZONTAL_PADDING, PAGE_VERTICAL_PADDING,
            PAGE_HORIZONTAL_PADDING));
        return p;
    }
}
