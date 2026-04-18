// utils/ConfigReader.java
package com.framework.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.openqa.selenium.Dimension;

public class ConfigReader {

    private static Properties properties;
    private static final String CONFIG_PATH =
        "src/test/resources/config.properties";

    // Load once, reuse everywhere
    static {
        try {
            properties = new Properties();
            FileInputStream fis = new FileInputStream(CONFIG_PATH);
            properties.load(fis);
            fis.close();
            System.out.println("✅ Config loaded");
        } catch (IOException e) {
            throw new RuntimeException("❌ Cannot load config: " + e.getMessage());
        }
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("❌ Key not found in config: " + key);
        }
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static Dimension getDimension(String key) {
        String value = get(key);
        String[] parts = value.split(",");
        if (parts.length != 2) {
            throw new RuntimeException("❌ Invalid dimension format for key: " + key);
        }
        try {
            int width = Integer.parseInt(parts[0].trim());
            int height = Integer.parseInt(parts[1].trim());
            return new Dimension(width, height);
        } catch (NumberFormatException e) {
            throw new RuntimeException("❌ Invalid number format in dimension for key: " + key);
        }
    }   
}