package com.framework.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.Set;

public class WindowHelper {

    private static final Logger log = LoggerFactory.getLogger(WindowHelper.class);

    private final WebDriver     driver;
    private final WebDriverWait wait;
    private String              parentWindow;

    public WindowHelper(WebDriver driver) {
        this.driver       = driver;
        this.wait         = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.parentWindow = driver.getWindowHandle();
        log.info("WindowHelper initialized — parent handle: [{}]", parentWindow);
    }

    /**
     * Waits for a new window to appear and switches to it.
     * Useful when a click opens a new tab or popup.
     */
    public void switchToNewWindow() {
        log.info("Waiting for new window to open...");

        // Wait until a new window handle appears
        wait.until(driver -> driver.getWindowHandles().size() > 1);

        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(parentWindow)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
                log.info("Switched to new window: [{}]", handle);
                return;
            }
        }
        throw new WindowNotFoundException("No new window found after waiting");
    }

    /**
     * Switches to a window matching the given title.
     * Useful when multiple windows are open simultaneously.
     *
     * @param title Exact window title to match
     */
    public void switchToWindowByTitle(String title) {
        log.info("Switching to window with title: [{}]", title);

        wait.until(driver -> {
            for (String handle : driver.getWindowHandles()) {
                driver.switchTo().window(handle);
                if (driver.getTitle().equals(title)) return true;
            }
            return false;
        });

        log.info("Switched to window: [{}]", title);
    }

    /**
     * Closes the current window and switches back to parent.
     */
    public void closeCurrentWindowAndSwitchBack() {
        String current = driver.getWindowHandle();
        log.info("Closing window [{}] and returning to parent [{}]", current, parentWindow);

        driver.close();
        driver.switchTo().window(parentWindow);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        log.info("Returned to parent window");
    }

    /**
     * Switches back to parent window without closing the current one.
     * Use when you need to return to parent while keeping child open.
     */
    public void switchBackToParent() {
        log.info("Switching back to parent window: [{}]", parentWindow);
        driver.switchTo().window(parentWindow);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
    }

    /**
     * Refreshes the stored parent handle.
     * Call this if the parent window has been replaced (e.g. after redirect).
     */
    public void refreshParentHandle() {
        parentWindow = driver.getWindowHandle();
        log.info("Parent handle refreshed: [{}]", parentWindow);
    }

    public String getParentHandle() {
        return parentWindow;
    }

    public int getOpenWindowCount() {
        return driver.getWindowHandles().size();
    }

    // -------------------------------------------------------------------------
    // Custom exception
    // -------------------------------------------------------------------------

    public static class WindowNotFoundException extends RuntimeException {
        public WindowNotFoundException(String message) {
            super(message);
        }
    }
}