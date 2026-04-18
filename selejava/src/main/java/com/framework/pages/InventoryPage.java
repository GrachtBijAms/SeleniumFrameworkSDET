package com.framework.pages;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.framework.base.BasePage;

public class InventoryPage extends BasePage {

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath = "//select[@data-test='product-sort-container']")
    protected WebElement filter;

    @FindBy(xpath = "//div[@data-test='inventory-item-price']")
    protected List<WebElement> productPrices;

    public void selectFilterOption(String option) {
        Select select = new Select(filter);
        select.selectByVisibleText(option);
    }

    public List<String> getProductPrices() {
        String[] prices = new String[productPrices.size()];
        for (int i = 0; i < productPrices.size(); i++) {
            prices[i] = productPrices.get(i).getText();
        }
        return Arrays.asList(prices);
    }
    
}
