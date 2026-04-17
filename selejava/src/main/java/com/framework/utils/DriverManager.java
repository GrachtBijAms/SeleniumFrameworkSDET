package com.framework.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class DriverManager {

    private static final Logger log = LoggerFactory.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();

    private DriverManager() {}

    public static WebDriver getDriver() {
        WebDriver driver = driverThread.get();
        if (driver == null) {
            throw new IllegalStateException(
                "WebDriver not initialized for thread: " + Thread.currentThread().getName()
            );
        }
        return driver;
    }

    public static void initDriver() {
        // Guard: prevent orphaned driver if called twice on same thread
        if (driverThread.get() != null) {
            log.warn("Driver already initialized on thread [{}] — quitting existing before reinitializing",
                Thread.currentThread().getName());
            quitDriver();
        }

        String browser = ConfigReader.get("browser");
        boolean headless = ConfigReader.getBoolean("headless");

        WebDriver driver = createDriver(browser, headless);

        // Page load timeout only — no implicit wait (conflicts with WebDriverWait)
        driver.manage().timeouts()
            .pageLoadTimeout(
                Duration.ofSeconds(ConfigReader.getInt("page.load.timeout"))
            );

        driverThread.set(driver);
        log.info("Driver initialized: browser=[{}] headless=[{}] thread=[{}]",
            browser, headless, Thread.currentThread().getName());
    }

    public static void quitDriver() {
        WebDriver driver = driverThread.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("Driver closed on thread [{}]", Thread.currentThread().getName());
            } catch (Exception e) {
                log.warn("Exception while quitting driver: {}", e.getMessage());
            } finally {
                driverThread.remove();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static WebDriver createDriver(String browser, boolean headless) {
        return switch (browser.toLowerCase()) {
            case "chrome"  -> buildChromeDriver(headless);
            case "firefox" -> buildFirefoxDriver(headless);
            default -> throw new IllegalArgumentException(
                "Unsupported browser: '" + browser + "'. Valid values: chrome, firefox"
            );
        };
    }

    private static WebDriver buildChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-extensions");
        if (headless) options.addArguments("--headless=new");
        return new ChromeDriver(options);
    }

    private static WebDriver buildFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (headless) options.addArguments("--headless");
        return new FirefoxDriver(options);
    }
}