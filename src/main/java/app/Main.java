package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

final class Main {
    private static final int LOADING_TEXT_RED = 99;
    private static final int LOADING_TEXT_GREEN = 107;
    private static final int LOADING_TEXT_BLUE = 108;
    private static final float LOADING_TITLE_FONT_SIZE = 20f;
    private static final int TITLE_COLOR_BLUE = 165;
    private static final int TITLE_COLOR_GREEN = 95;
    private static final int TITLE_COLOR_RED = 24;
    private static final int LOADING_HORIZONTAL_PADDING = 30;
    private static final int LOADING_VERTICAL_PADDING = 22;
    private static final int BORDER_COLOR_BLUE = 218;
    private static final int BORDER_COLOR_GREEN = 226;
    private static final int BORDER_COLOR_RED = 228;
    private Main() {
    }

    /**
     * Starts the desktop application.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (final Exception ignored) {
            }
            UIManager.put("Button.background", Color.WHITE);
            UIManager.put("Button.foreground", Color.BLACK);
            final JWindow loading = loadingWindow();
            loading.setVisible(true);
            try {
                new AppBuilder().buildAndSeed(app -> {
                    loading.dispose();
                    app.setLocationRelativeTo(null);
                    app.setVisible(true);
                });
            }
            catch (final RuntimeException failure) {
                loading.dispose();
                JOptionPane.showMessageDialog(null, failure.getMessage(), "FlushID could not start",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static JWindow loadingWindow() {
        final JWindow window = new JWindow();
        final JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(BORDER_COLOR_RED,
            BORDER_COLOR_GREEN, BORDER_COLOR_BLUE)),
            BorderFactory.createEmptyBorder(LOADING_VERTICAL_PADDING, LOADING_HORIZONTAL_PADDING,
                LOADING_VERTICAL_PADDING, LOADING_HORIZONTAL_PADDING)));
        final JLabel title = new JLabel("FlushID");
        title.setForeground(new Color(TITLE_COLOR_RED, TITLE_COLOR_GREEN, TITLE_COLOR_BLUE));
        title.setFont(title
            .getFont()
            .deriveFont(Font.BOLD, LOADING_TITLE_FONT_SIZE));
        final JLabel message = new JLabel("Loading washrooms and live status...");
        message.setForeground(new Color(LOADING_TEXT_BLUE, LOADING_TEXT_GREEN, LOADING_TEXT_RED));
        content.add(title, BorderLayout.NORTH);
        content.add(message, BorderLayout.CENTER);
        window.setContentPane(content);
        window.pack();
        window.setLocationRelativeTo(null);
        return window;
    }
}
