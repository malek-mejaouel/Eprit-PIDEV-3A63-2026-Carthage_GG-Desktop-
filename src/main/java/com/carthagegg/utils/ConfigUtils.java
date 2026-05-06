package com.carthagegg.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtils {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigUtils.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Warning: config.properties not found in classpath.");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            System.err.println("Error loading config.properties: " + ex.getMessage());
        }
    }

    /**
     * Get a property value by key.
     * @param key The property key.
     * @return The property value, or null if not found.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get a property value by key with a default value.
     * @param key The property key.
     * @param defaultValue The default value if key is not found.
     * @return The property value or defaultValue.
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
