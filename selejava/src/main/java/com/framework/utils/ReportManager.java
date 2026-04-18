package com.framework.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportManager {

    private static final Logger log = LoggerFactory.getLogger(ReportManager.class);

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    private ReportManager() {}

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    public static void initReports() {
        String reportPath = ConfigReader.get("reports.path") + "TestReport.html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setDocumentTitle("SDET Test Report");
        spark.config().setReportName("Automation Results");
        spark.config().setEncoding("utf-8");
        spark.config().setTimelineEnabled(false);

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Framework",   "Selenium + TestNG");
        extent.setSystemInfo("OS",          System.getProperty("os.name"));
        extent.setSystemInfo("Environment", ConfigReader.get("env"));
        extent.setSystemInfo("Browser",     ConfigReader.get("browser"));
        extent.setSystemInfo("Tester",      "Test Automation Engineer");

        log.info("ExtentReport initialized: {}", reportPath);
    }

    public static void flushReports() {
        if (extent != null) {
            extent.flush();
            log.info("ExtentReport saved");
        }
    }

    // -------------------------------------------------------------------------
    // Test node
    // -------------------------------------------------------------------------

    public static void createTest(String testName) {
        ExtentTest extentTest = extent.createTest(testName);
        test.set(extentTest);
        log.info("Test created in report: {}", testName);
    }

    public static ExtentTest getTest() {
        ExtentTest extentTest = test.get();
        if (extentTest == null) {
            throw new IllegalStateException(
                "ExtentTest not initialized for thread: "
                + Thread.currentThread().getName()
            );
        }
        return extentTest;
    }

    // -------------------------------------------------------------------------
    // Logging
    // -------------------------------------------------------------------------

    public static void logPass(String message) {
        getTest().pass(message);
    }

    public static void logFail(String message) {
        getTest().fail(message);
    }

    public static void logInfo(String message) {
        getTest().info(message);
    }

    public static void logSkip(String message) {
        getTest().skip(message);
    }

    /**
     * Logs error details as a child node under the failed test.
     * Use this after logFail() to show the exception message separately.
     */
    public static void logError(String message) {
        getTest()
            .createNode("Error Detail")
            .fail("<b style='color:red'>Error:</b> " + message);
    }

    // -------------------------------------------------------------------------
    // Screenshot
    // -------------------------------------------------------------------------

    public static void logScreenshot(String path) {
        if (path == null) {
            log.warn("Screenshot path is null — skipping attachment");
            return;
        }
        try {
            getTest().addScreenCaptureFromPath(path);
        } catch (Exception e) {
            log.error("Could not attach screenshot to report: {}", e.getMessage());
        }
    }
}