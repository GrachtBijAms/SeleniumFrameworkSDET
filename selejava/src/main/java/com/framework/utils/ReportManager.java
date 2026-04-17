    // utils/ReportManager.java
    package com.framework.utils;

    import com.aventstack.extentreports.*;
    import com.aventstack.extentreports.reporter.ExtentSparkReporter;
    import com.aventstack.extentreports.reporter.configuration.Theme;

    public class ReportManager {

        private static ExtentReports extent;
        private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();

        public static void initReports() {
            String reportPath = ConfigReader.get("reports.path")
                + "TestReport.html";

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setDocumentTitle("SDET Test Report");
            spark.config().setReportName("Automation Results");
            spark.config().setEncoding("utf-8");
            spark.config().setTimelineEnabled(false);

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Framework",    "Selenium + TestNG");
            extent.setSystemInfo("OS",        System.getProperty("os.name"));
            extent.setSystemInfo("Environment",  ConfigReader.get("env"));
            extent.setSystemInfo("Browser",      ConfigReader.get("browser"));
            extent.setSystemInfo("Tester",       "Test Automation Engineer");

            System.out.println("✅ Report initialized");
        }

        public static void createTest(String testName) {
            ExtentTest extentTest = extent.createTest(testName);
            test.set(extentTest);
        }

        public static ExtentTest getTest() {
            return test.get();
        }

        public static void flushReports() {
            if (extent != null) {
                extent.flush();
                System.out.println("📊 Report saved");
            }
        }

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


        public static void ErrorComponent(String failure) {
            getTest().createNode("Error Message").fail(failure);
        }

        public static void logScreenshot(String path) {
            try {
                getTest().addScreenCaptureFromPath(path);
            } catch (Exception e) {
                System.out.println("❌ Could not attach screenshot to report");
            }
        }
    }