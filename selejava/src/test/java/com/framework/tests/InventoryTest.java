package com.framework.tests;

import org.testng.annotations.Test;
import com.framework.annotations.TestCaseName;
import com.framework.constants.AppConstants;
import com.framework.pages.InventoryPage;
import com.framework.pages.LoginPage;
import com.framework.utils.ScreenshotUtil;

public class InventoryTest extends BaseTest {


    @Test()
    @TestCaseName("TC-001: Verify Filter Functionality on Inventory Page")
    public void filterTest() {
        // This is a placeholder test method. You can implement your filter test here.
        // For example, you can create an instance of your InventoryPage and call the selectFilterOption method.
            LoginPage loginPage = new LoginPage(getDriver());
            InventoryPage inventoryPage = new InventoryPage(getDriver());
            loginPage.navigateToLoginPage();
            loginPage.login(AppConstants.VALID_USERNAME, AppConstants.VALID_PASSWORD);
            pdfReport.addStep("Logged in with standard_user credentials");
            pdfReport.addScreenshot(ScreenshotUtil.capture("Login Success"), "Successfully logged in with standard_user credentials");
            

            inventoryPage.selectFilterOption("Price (low to high)");
            pdfReport.addStep("Selected filter option: Price (low to high)");
            pdfReport.addScreenshot(ScreenshotUtil.capture("Filter Applied"), "Applied filter: Price (low to high)");
            // Add assertions here to verify that the filter is applied correctly.


            inventoryPage.getProductPrices().forEach(price -> {
                System.out.println(price);
            });
    }

    
}
