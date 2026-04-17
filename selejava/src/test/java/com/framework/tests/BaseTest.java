    package com.framework.tests;

    import com.framework.utils.DriverManager;
    import com.framework.utils.ReportManager;
    import com.framework.utils.ScreenshotUtil;
    import com.framework.utils.ScreenshotPdfReport;
    import org.openqa.selenium.WebDriver;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.testng.ITestResult;
    import org.testng.annotations.AfterMethod;
    import org.testng.annotations.AfterSuite;
    import org.testng.annotations.BeforeMethod;
    import org.testng.annotations.BeforeSuite;
    import java.lang.reflect.Method;  // ← Method

    public class BaseTest {

        private static final Logger log = LoggerFactory.getLogger(BaseTest.class);
        protected ScreenshotPdfReport pdfReport;

        @BeforeSuite
        public void beforeSuite() {
            pdfReport = new ScreenshotPdfReport("TestExecutionReport.pdf");
            ReportManager.initReports();
            log.info("Starting test suite execution");
        }

        @AfterSuite
        public void afterSuite() {
            pdfReport.generate();
            ReportManager.flushReports();
            log.info("Test suite execution completed");
        }



        @BeforeMethod
        public void setUp(Method method) {
            DriverManager.initDriver();
            pdfReport.addTestCaseTitle(method.getName());
            ReportManager.createTest(method.getName());
            ReportManager.logInfo("Test Started - " + method.getName());

            log.info("Test started: {}", method.getName());
        }

        @AfterMethod
        public void tearDown(ITestResult result) {
            if (result.getStatus() == ITestResult.FAILURE) {
                String path = ScreenshotUtil.capture("FAILED_" + result.getName());
                pdfReport.addScreenshot(path, "Test Failed — final state");
                pdfReport.markFailed();
                ReportManager.logScreenshot(path);
                ReportManager.logFail("Test Failed - " + result.getName());
                ReportManager.ErrorComponent(result.getThrowable().getMessage());
            }else if(result.getStatus() == ITestResult.SUCCESS){
                pdfReport.markPassed();
                ReportManager.logPass("Test Passed - " + result.getName());
            }else if(result.getStatus() == ITestResult.SKIP){
                pdfReport.markSkipped();
                ReportManager.logSkip("Test Skipped - " + result.getName());
            }
            //pdfReport.generate();
            DriverManager.quitDriver();
        } 

        protected WebDriver getDriver() {
            return DriverManager.getDriver();
        }
    }