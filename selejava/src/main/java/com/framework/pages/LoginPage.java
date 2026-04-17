package com.framework.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.framework.base.BasePage;
import com.framework.constants.AppConstants;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(id = "user-name")
    protected WebElement usernameInput;

    @FindBy(id = "password")
    protected WebElement passwordInput;

    @FindBy(id = "login-button")
    protected WebElement loginButton;

    @FindBy(css = ".error-message-container.error")
    protected WebElement errorMessage;

    @FindBy(xpath = "//div[@class='app_logo']")
    protected WebElement loginHeader;

    /**
     * Navigates to the login page.
     */
    public void navigateToLoginPage() {
        navigateTo(AppConstants.BASE_URL);
    }

    /**
     * Performs login action with the provided username and password.   
     * @param username  // The username to be entered in the login form.
     * @param password // The password to be entered in the login form.
     */
    public void login(String username, String password) {
        type(usernameInput, username);
        type(passwordInput, password);
        loginButton.click();
    }

    public String getErrorMessage() {
        return errorMessage.getText();
    }

    public boolean isLoginHeaderDisplayed() {
        return loginHeader.isDisplayed();
    }

}
