// base/BasePage.java
package com.framework.base;

import com.framework.utils.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class BasePage {

    protected WebDriver      driver;
    protected WebDriverWait  wait;
    protected JavascriptExecutor js;

    public BasePage(WebDriver driver) {
        this.driver = DriverManager.getDriver();
        this.wait   = new WebDriverWait(driver,
            Duration.ofSeconds(15));
        this.js     = (JavascriptExecutor) driver;
        PageFactory.initElements(driver, this);
    }

    
    // ── Navigation ────────────────────────────────────────────────────────
    /**
     * Navigates to the specified URL and waits for the page to load.
     * @param url The URL to navigate to
     */
    protected void navigateTo(String url) {
        driver.get(url);
        waitForPageLoad();
    }

    /**
     * Waits for the page to load completely.
     */
    protected void waitForPageLoad() {
        wait.until(d ->
            js.executeScript("return document.readyState")
              .equals("complete")
        );
    }

    // ── Find Elements ─────────────────────────────────────────────────────
    /**
     * Finds a single visible element using the given locator.  
     * @param locator The locator of the element to find 
     * @return The visible WebElement matching the locator
     */
    protected WebElement find(By locator) {
        Objects.requireNonNull(locator, "Locator cannot be null");
        try {
            return wait.until(
                ExpectedConditions.visibilityOfElementLocated(locator)
            );
        } catch (TimeoutException e) {
            throw new NoSuchElementException(
                "Element not found or not visible: " + locator, e
            );
        }
    }


    /**
     * Returns a list of visible elements matching the locator.
     * @param locator The locator of the elements to find
     * @return A list of visible WebElements matching the locator
     */
    protected List<WebElement> findAll(By locator) {
        return wait.until(
            ExpectedConditions.visibilityOfAllElementsLocatedBy(locator)
        );
    }

    // ── Actions ───────────────────────────────────────────────────────────
    
    /**
     * Clicks the element identified by the given locator.
     * @param locator The locator of the element to click
     */
    protected void click(By locator) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator))
            .click();
        } catch (ElementClickInterceptedException e) {
            // If normal click fails, try JavaScript click as a fallback
            jsClick(locator);
        }
        
    }

    /**
     * Clicks the given WebElement.
     * @param element The WebElement to click
     */
    protected void click(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element))
            .click();
        } catch (ElementClickInterceptedException e) {
            // If normal click fails, try JavaScript click as a fallback
            js.executeScript("arguments[0].click();", element);
        }
    }

    /**
     * Types the specified text into the element identified by the given locator.
     * @param locator The locator of the element to type into
     * @param text The text to type
     */
    protected void type(By locator, String text) {
        WebElement el = find(locator);
        el.clear();
        el.sendKeys(text);
    }

    /**
     * Types the specified text into the given WebElement.
     * @param element The WebElement to type into
     * @param text The text to type
     */
    protected void type(WebElement element, String text) {
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Gets the text of the element identified by the given locator.
     * This method returns null if the element is not found or not visible within the timeout.
     * @param locator The locator of the element to check
     * @return The text of the element, or null if not found
     */
    protected String getText(By locator) {
        try {
            return find(locator).getText();
        } catch (TimeoutException | NoSuchElementException e) {
            return null;
        }
    }   
    
    /**
     * Gets the value of the specified attribute for the element identified by the given locator.
     * This method returns null if the element is not found or not visible within the timeout.
     * @param locator The locator of the element to check
     * @param attribute The attribute to get
     * @return The value of the attribute, or null if not found
     */
    protected String getAttribute(By locator, String attribute) {
        return find(locator).getAttribute(attribute);
    }

    /**
     * Checks if the element identified by the given locator is visible.
     * This method returns false if the element is not found or not visible within the timeout.
     * @param locator The locator of the element to check
     * @return true if the element is visible, false otherwise
     */
    protected boolean isVisible(By locator) {
        try {
            return find(locator).isDisplayed();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Checks if the element identified by the given locator is enabled.
     * This method returns false if the element is not found or not visible within the timeout.
     * @param locator The locator of the element to check
     * @return true if the element is enabled, false otherwise
     */
    protected boolean isEnabled(By locator) {
        try {
            return find(locator).isEnabled();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Checks if the element identified by the given locator is selected.
     * This method returns false if the element is not found or not visible within the timeout.
     * @param locator The locator of the element to check
     * @return true if the element is selected, false otherwise
     */
    protected boolean isSelected(By locator) {
        try {
            return find(locator).isSelected();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Checks if the element identified by the given locator is present in the DOM.
     * This method does not wait for visibility and returns false if the element is not found.
     * @param locator The locator of the element to check
     * @return true if the element is present, false otherwise
     */
    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Scrolls to the element identified by the given locator using Actions class.
     * This method returns false if the element is not found or not visible within the timeout.     
     * @param locator The locator of the element to scroll to
     * @return true if the element is scrolled to, false otherwise
     */
    protected boolean scrollToElement(By locator) {
        try {
            Actions actions = new Actions(driver);
            WebElement element = find(locator);
            actions.scrollToElement(element).perform();
            
            return true;
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }

    // ── JavaScript Helpers ────────────────────────────────────────────────
    /**
     * Clicks the element identified by the given locator using JavaScript.
     * This is a fallback method when normal click fails due to intercepting elements.
     * @param locator The locator of the element to click
     */
    protected void jsClick(By locator) {
        js.executeScript("arguments[0].click();", find(locator));
    }

    /**
     * Scrolls the element identified by the given locator into view.
     * @param locator The locator of the element to scroll to
     */
    protected void jsscrollTo(By locator) {
        js.executeScript(
            "arguments[0].scrollIntoView({block:'center'});",
            find(locator)
        );
    }

    /**
     * Scrolls to the bottom of the page.
     */
    protected void scrollToBottom() {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    // ── Wait Helpers ──────────────────────────────────────────────────────
    
    /**
     * Waits for the element identified by the given locator to be visible.
     * @param locator The locator of the element to wait for
     */
    protected void waitForVisible(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits for the element identified by the given locator to be invisible.
     * @param locator The locator of the element to wait for
     */
    protected void waitForInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Waits for the element identified by the given locator to be clickable.
     * @param locator The locator of the element to wait for
     */    
    protected void waitForClickable(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }


    protected void waitForUrl(String urlFragment) {
        wait.until(ExpectedConditions.urlContains(urlFragment));
    }
}