package com.framework.tests;



import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.Assert;
import com.framework.constants.AppConstants;
import com.framework.pages.LoginPage;
import com.framework.utils.ScreenshotUtil;
/**
 * Unit test for simple App.
 */
public class AppTest extends BaseTest{   

    @Test
    public void loginTest() {
        // This is a placeholder test method. You can implement your login test here.
        // For example, you can create an instance of your LoginPage and call the login method.
         LoginPage loginPage = new LoginPage(getDriver());
         loginPage.navigateToLoginPage();
          pdfReport.addStep("Navigated to login page"); 
         pdfReport.addScreenshot(ScreenshotUtil.capture("Navigate-to-LoginPage"), "Navigate to Login Page");
         
         loginPage.login(AppConstants.VALID_USERNAME, AppConstants.VALID_PASSWORD);
         pdfReport.addStep("Entered Valid Credentials");

        pdfReport.addStep("Login Attempted");
         Assert.assertTrue(loginPage.isLoginHeaderDisplayed(), "Login header is not displayed, login might have failed.");
         pdfReport.addScreenshot(ScreenshotUtil.capture("Login Success"), "Success Login Test Screenshot");

        }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        return new Object[][] {
            {"invalid_user", "invalid_password","Epic sadface: Username and password do not match any user in this service"},
            {"", "invalid_password","Epic sadface: Username is required"},
            {"invalid_user", "","Epic sadface: Password is required"},
            {"", "","Epic sadface: Username is required"},
            {"s", "s","Epic sadface: Username is too short"}
        };
    }

    @Test(dataProvider = "invalidCredentials")
    public void isValidLoginTest(String username, String password, String expectedMessage) {
        // This is a placeholder test method. You can implement your invalid login test here.
        // For example, you can create an instance of your LoginPage and call the login method with invalid credentials.
         LoginPage loginPage = new LoginPage(getDriver());
         pdfReport.addStep("Navigated to login page"); 
         loginPage.navigateToLoginPage();
         
         pdfReport.addScreenshot(ScreenshotUtil.capture("Navigate-to-LoginPage"), "Navigate to Login Page");
         
         pdfReport.addStep("Attempted login with invalid credentials: " + username + " / " + password);
         loginPage.login(username, password);
         
         Assert.assertEquals(loginPage.getErrorMessage(), expectedMessage, "Error message does not match expected message for invalid login.");
         pdfReport.addScreenshot(ScreenshotUtil.capture("invalidLoginTest"), "Invalid Login Test Screenshot");
             pdfReport.addStep("Verified the error message");
    } 

}
