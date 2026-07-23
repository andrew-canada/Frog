package app;

import javax.swing.*;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            JFrame app = new AppBuilder().build();
            app.setLocationRelativeTo(null);
            app.setVisible(true);
        });
    }
}
