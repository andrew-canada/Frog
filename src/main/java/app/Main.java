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

public final class Main {
    private Main() {
    }

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
        content.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(228, 226, 218)),
            BorderFactory.createEmptyBorder(22, 30, 22, 30)));
        final JLabel title = new JLabel("FlushID");
        title.setForeground(new Color(24, 95, 165));
        title.setFont(title
            .getFont()
            .deriveFont(Font.BOLD, 20f));
        final JLabel message = new JLabel("Loading washrooms and live status…");
        message.setForeground(new Color(108, 107, 99));
        content.add(title, BorderLayout.NORTH);
        content.add(message, BorderLayout.CENTER);
        window.setContentPane(content);
        window.pack();
        window.setLocationRelativeTo(null);
        return window;
    }
}
