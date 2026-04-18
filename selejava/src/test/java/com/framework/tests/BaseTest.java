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
    import org.testng.annotations.AfterMethod;
    import org.testng.annotations.AfterSuite;
    import org.testng.annotations.BeforeMethod;
    import org.testng.annotations.BeforeSuite;
    import java.lang.reflect.Method;  // ← Method

    public class BaseTest implements ITest{

        private final ThreadLocal<String> testName = new ThreadLocal<>();   
        private static final Logger log = LoggerFactory.getLogger(BaseTest.class);
        protected ScreenshotPdfReport pdfReport;

        @BeforeMethod(alwaysRun = true)
        public void setTestName(Method method) {
            String name = method.getName();
            if (method.isAnnotationPresent(com.framework.annotations.TestCaseName.class)) {
                name = method.getAnnotation(com.framework.annotations.TestCaseName.class).value();
            }
            testName.set(name);
        }   

        @Override
        public String getTestName() {
            // This method can be enhanced to return a more descriptive name based on annotations or method names
            return testName.get(); // Return the set test name
        }


        @BeforeSuite
        public void beforeSuite() {
            pdfReport = new ScreenshotPdfReport("FullTestExecutionReport.pdf");
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
            pdfReport.addTestCaseTitle(testName.get());
            ReportManager.createTest(testName.get());
            ReportManager.logInfo("Test Started - " + testName.get());

            log.info("Test started: {}", testName.get());
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