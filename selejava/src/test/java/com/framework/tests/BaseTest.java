package com.framework.tests;

import com.framework.utils.DriverManager;
import com.framework.utils.ReportManager;
import com.framework.utils.ScreenshotUtil;
import com.framework.utils.ScreenshotPdfReport;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITest;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;

public class BaseTest implements ITest {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    private final ThreadLocal<String> testName = new ThreadLocal<>();

    // Instance field — each test class gets its own PDF
    protected ScreenshotPdfReport pdfReport;

    // -------------------------------------------------------------------------
    // ITest
    // -------------------------------------------------------------------------

    @Override
    public String getTestName() {
        String name = testName.get();
        return (name != null) ? name : "UnknownTest";
    }

    // -------------------------------------------------------------------------
    // Suite level — ExtentReport only, runs once
    // -------------------------------------------------------------------------

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        ReportManager.initReports();
        log.info("Test suite started");
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        ReportManager.flushReports();
        log.info("Test suite completed");
    }

    // -------------------------------------------------------------------------
    // Class level — one PDF per test class
    // -------------------------------------------------------------------------

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        // getSimpleName() gives "AppTest", "InventoryTest" etc.
        String className = getClass().getSimpleName();
        pdfReport = new ScreenshotPdfReport(className);
        log.info("PDF report initialized for class: {}", className);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        if (pdfReport != null) {
            String path = pdfReport.generate();
            log.info("PDF report saved for class [{}]: {}",
                getClass().getSimpleName(), path);
        }
    }

    // -------------------------------------------------------------------------
    // Method level — runs per test
    // -------------------------------------------------------------------------

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method, Object[] testData) {
        // Resolve display name
        String name = method.getName();
        if (method.isAnnotationPresent(
                com.framework.annotations.TestCaseName.class)) {
            name = method.getAnnotation(
                com.framework.annotations.TestCaseName.class).value();
        } else if (testData != null && testData.length > 0) {
            name = String.valueOf(testData[0]);
        }
        testName.set(name);

        // Driver
        DriverManager.initDriver();

        // Reports
        pdfReport.addTestCaseTitle(getTestName());
        ReportManager.createTest(getTestName());
        ReportManager.logInfo("Test Started: " + getTestName());

        log.info("Test started: {}", getTestName());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            String path = ScreenshotUtil.capture("FAILED_" + getTestName());
            pdfReport.addScreenshot(path, "Failure Screenshot — " + getTestName());
            pdfReport.markFailed();
            ReportManager.logScreenshot(path);
            ReportManager.logFail("Test Failed: " + getTestName());
            ReportManager.logError(result.getThrowable().getMessage());

        } else if (result.getStatus() == ITestResult.SUCCESS) {
            pdfReport.markPassed();
            ReportManager.logPass("Test Passed: " + getTestName());

        } else if (result.getStatus() == ITestResult.SKIP) {
            pdfReport.markSkipped();
            ReportManager.logSkip("Test Skipped: " + getTestName());
        }

        DriverManager.quitDriver();
        testName.remove();
        log.info("Test finished: {}", result.getName());
    }

    // -------------------------------------------------------------------------
    // Accessor
    // -------------------------------------------------------------------------

    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }
}