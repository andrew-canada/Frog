package configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Resolves application configuration from the process environment or the bundled environment file.
 */
public final class ApplicationEnvironment {
    private static final String RESOURCE = "/environment_variables.env";
    private static final Properties BUNDLED_VALUES = bundledValues();

    private ApplicationEnvironment() {
    }

    /**
     * Returns the non-blank process environment value when present, otherwise the bundled value.
     *
     * @param key configuration key.
     * @return the configured value, or {@code null} when it is not configured.
     */
    public static String value(final String key) {
        return resolve(key, System.getenv(key), BUNDLED_VALUES);
    }

    static String resolve(final String key, final String environmentValue, final Properties bundledValues) {
        final String result;
        if (environmentValue == null || environmentValue.isBlank()) {
            result = bundledValues.getProperty(key);
        }
        else {
            result = environmentValue;
        }
        return result;
    }

    private static Properties bundledValues() {
        final InputStream input = ApplicationEnvironment.class.getResourceAsStream(RESOURCE);
        if (input == null) {
            throw new IllegalStateException("Missing bundled " + RESOURCE + " configuration file.");
        }
        final Properties values = new Properties();
        try (input) {
            values.load(input);
            return values;
        }
        catch (final IOException failure) {
            throw new IllegalStateException("Could not read bundled " + RESOURCE + " configuration file.", failure);
        }
    }
}
