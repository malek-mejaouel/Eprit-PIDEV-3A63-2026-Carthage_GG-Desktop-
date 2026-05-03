package com.carthagegg.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static final Properties properties = new Properties();

    static {
        String[] locations = {"config.properties", "src/main/resources/config.properties"};
        boolean loaded = false;

        // 1. Try loading from file system
        for (String loc : locations) {
            File file = new File(loc);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    properties.load(fis);
                    System.out.println("ConfigManager: Loaded configuration from file: " + file.getAbsolutePath());
                    loaded = true;
                    break;
                } catch (IOException e) {
                    System.err.println("ConfigManager: Error loading file " + loc + " - " + e.getMessage());
                }
            }
        }

        // 2. Fallback to classpath resource
        if (!loaded) {
            try (InputStream is = ConfigManager.class.getResourceAsStream("/config.properties")) {
                if (is != null) {
                    properties.load(is);
                    System.out.println("ConfigManager: Loaded configuration from classpath resource /config.properties");
                    loaded = true;
                }
            } catch (IOException e) {
                System.err.println("ConfigManager: Error loading classpath resource - " + e.getMessage());
            }
        }

        if (!loaded) {
            System.err.println("ConfigManager: WARNING: config.properties not found in any location!");
        }
    }

    public static String get(String key) {
        String val = properties.getProperty(key);
        return val != null ? val.trim() : null;
    }

    public static String get(String key, String defaultValue) {
        String val = properties.getProperty(key);
        return val != null ? val.trim() : defaultValue;
    }
}
