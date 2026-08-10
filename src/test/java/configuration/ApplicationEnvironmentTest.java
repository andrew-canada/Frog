package configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Properties;

import org.junit.jupiter.api.Test;

class ApplicationEnvironmentTest {
    @Test
    void bundledConfigurationIsAvailableThroughThePublicLookup() {
        final String graphhopperKey = ApplicationEnvironment.value("GRAPHHOPPER_API_KEY");

        assertFalse(graphhopperKey == null || graphhopperKey.isBlank());
    }

    @Test
    void processEnvironmentTakesPrecedenceOverBundledValue() {
        final Properties bundled = new Properties();
        bundled.setProperty("TEST_KEY", "bundled");

        assertEquals("process", ApplicationEnvironment.resolve("TEST_KEY", "process", bundled));
    }

    @Test
    void bundledValuesAreUsedWhenTheProcessValueIsBlankOrMissing() {
        final Properties bundled = new Properties();
        bundled.setProperty("TEST_KEY", "bundled");

        assertEquals("bundled", ApplicationEnvironment.resolve("TEST_KEY", "", bundled));
        assertEquals("bundled", ApplicationEnvironment.resolve("TEST_KEY", null, bundled));
        assertNull(ApplicationEnvironment.resolve("MISSING", null, bundled));
    }
}
