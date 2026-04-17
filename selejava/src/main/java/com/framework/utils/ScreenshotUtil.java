package com.framework.utils;

import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.framework.constants.AppConstants;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotUtil.class);
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"); // SSS = milliseconds

    private ScreenshotUtil() {}

    /**
     * Captures a screenshot using the current thread's WebDriver.
     *
     * @param stepName Label used in the filename e.g. "login_success"
     * @return Absolute path to saved screenshot, or null on failure
     */
    public static String capture(String stepName) {
        WebDriver driver = DriverManager.getDriver();

        if (driver == null) {
            log.warn("Screenshot skipped — driver not initialized for thread [{}]",
                Thread.currentThread().getName());
            return null;
        }

        try {
            String folderPath = AppConstants.SCREENSHOTS_PATH;
            String timestamp  = LocalDateTime.now().format(FORMATTER);
            String fileName   = stepName + "_" + timestamp + ".png";
            Path   destination = Paths.get(folderPath, fileName);

            Files.createDirectories(destination.getParent());

            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(destination, bytes); // no temp file needed

            log.info("Screenshot captured: [{}] -> {}", stepName, destination);
            return destination.toAbsolutePath().toString();

        } catch (IOException e) {
            log.error("Screenshot failed for step [{}]: {}", stepName, e.getMessage());
            return null;
        }
    }
}