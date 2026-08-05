package app;

import javax.swing.*;
import java.awt.Color;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            UIManager.put("Button.background", Color.WHITE);
            UIManager.put("Button.foreground", Color.BLACK);
            try {
                JFrame app = new AppBuilder().build();
                app.setLocationRelativeTo(null);
                app.setVisible(true);
            } catch (RuntimeException failure) {
                JOptionPane.showMessageDialog(null, failure.getMessage(), "FlushID could not start", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
