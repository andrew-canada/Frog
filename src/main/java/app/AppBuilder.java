package app;

import java.util.function.Consumer;

import javax.swing.JFrame;

/** Composition-root facade used by the application entry point. */
final class AppBuilder {
    private static void showLoadedFrame(final JFrame frame) {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Builds the application from the configured data sources.
     * @return the application frame.
     */
    public JFrame build() {
        return compose(false, AppBuilder::showLoadedFrame);
    }

    /**
     * Builds the application and asynchronously verifies baseline data.
     * @return the application frame.
     */
    public JFrame buildAndSeed() {
        return compose(true, AppBuilder::showLoadedFrame);
    }

    /**
     * Builds the application and invokes a callback after startup data loads.
     * @param onLoaded callback invoked after loading.
     * @return the application frame.
     */
    public JFrame buildAndSeed(final Consumer<JFrame> onLoaded) {
        final Consumer<JFrame> callback;
        if (onLoaded == null) {
            callback = AppBuilder::showLoadedFrame;
        }
        else {
            callback = onLoaded;
        }
        return compose(true, callback);
    }

    private JFrame compose(final boolean seedData, final Consumer<JFrame> onLoaded) {
        return ApplicationComposition.build(seedData, onLoaded);
    }
}
